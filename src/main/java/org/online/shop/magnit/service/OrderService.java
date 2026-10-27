package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.*;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String email) {
        return orders.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream().map(OrderResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrderForUser(String email, Long id) {
        var order = orders.findById(id).orElseThrow(() -> ApiException.notFound("Заказ не найден"));
        checkOwner(order, email);
        return OrderResponse.from(order);
    }
    public OrderResponse createOrder(String email, CreateOrderRequest r) {
        if (r == null || r.items() == null || r.items().isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Заказ не может быть пустым");
        // Stable lock order avoids deadlocks between purchases containing the same products.
        Map<Long, Integer> quantities = new TreeMap<>();
        for (var item : r.items()) {
            if (item == null || item.productId() == null || item.quantity() == null || item.quantity() < 1 || item.quantity() > 10000)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Некорректная позиция заказа");
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }
        var order = new Order(); order.setUser(user(email));
        for (var entry : quantities.entrySet()) {
            var p = products.findLockedById(entry.getKey()).orElseThrow(() -> ApiException.notFound("Товар не найден"));
            int stock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            if (stock < entry.getValue()) throw ApiException.conflict("Недостаточно товара: " + p.getName());
            p.setStockQuantity(stock - entry.getValue());
            var item = new OrderItem(); item.setOrder(order); item.setProduct(p);
            item.setQuantity(entry.getValue()); item.setPriceAtPurchase(p.getPrice());
            order.getItems().add(item);
        }
        return OrderResponse.from(orders.save(order));
    }
    public OrderResponse updateStatus(Long id, Order.OrderStatus status) {
        var order = locked(id);
        transition(order, status);
        return OrderResponse.from(order);
    }
    public OrderResponse cancel(String email, Long id) {
        var order = locked(id); checkOwner(order, email);
        if (order.getStatus() != Order.OrderStatus.NEW && order.getStatus() != Order.OrderStatus.CANCELLED)
            throw ApiException.conflict("Самостоятельно можно отменить только новый заказ");
        transition(order, Order.OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }
    private Order locked(Long id) {
        return orders.findLockedById(id).orElseThrow(() -> ApiException.notFound("Заказ не найден"));
    }
    private User user(String email) {
        return users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
    }
    private void checkOwner(Order order, String email) {
        if (!order.getUser().getEmail().equals(email)) throw ApiException.notFound("Заказ не найден");
    }
    private void transition(Order o, Order.OrderStatus next) {
        var current = o.getStatus();
        if (current == next) return;
        boolean allowed = switch (current) {
            case NEW -> next == Order.OrderStatus.PAID || next == Order.OrderStatus.CANCELLED;
            case PAID -> next == Order.OrderStatus.SHIPPED || next == Order.OrderStatus.CANCELLED;
            case SHIPPED -> next == Order.OrderStatus.DELIVERED;
            default -> false;
        };
        if (!allowed) throw ApiException.conflict("Недопустимый переход статуса: " + current + " → " + next);
        if (next == Order.OrderStatus.CANCELLED) {
            o.getItems().stream().sorted(Comparator.comparing(i -> i.getProduct().getId())).forEach(i -> {
                var p = products.findLockedById(i.getProduct().getId()).orElseThrow();
                long stock = (long)p.getStockQuantity() + i.getQuantity();
                if (stock > Integer.MAX_VALUE) throw ApiException.conflict("Превышен допустимый остаток товара");
                p.setStockQuantity((int)stock);
            });
        }
        o.setStatus(next);
    }
}
