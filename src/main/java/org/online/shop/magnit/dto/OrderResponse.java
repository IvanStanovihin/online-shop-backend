package org.online.shop.magnit.dto;

import org.online.shop.magnit.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record OrderResponse(Long id, Long userId, LocalDateTime createdAt, Order.OrderStatus status,
                            List<Item> items, BigDecimal total) {
    public record Item(Long productId, String productName, Integer quantity, BigDecimal priceAtPurchase,
                       BigDecimal subtotal) {}
    public static OrderResponse from(Order order) {
        var items = order.getItems().stream().map(i -> new Item(i.getProduct().getId(),
            i.getProduct().getName(), i.getQuantity(), i.getPriceAtPurchase(),
            i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getCreatedAt(),
            order.getStatus(), items, items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
