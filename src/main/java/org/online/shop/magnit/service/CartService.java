package org.online.shop.magnit.service;

import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.*;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional
public class CartService {
    private final CartRepository carts;
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderService orders;
    @Transactional(readOnly = true)
    public CartResponse get(String email) {
        var user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        return carts.findByUserId(user.getId()).map(CartResponse::from).orElse(new CartResponse(List.of(), BigDecimal.ZERO));
    }
    public CartResponse put(String email, Long productId, int quantity) {
        var cart = lockedCart(email);
        var product = products.findById(productId).orElseThrow(() -> ApiException.notFound("Товар не найден"));
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity)
            throw ApiException.conflict("Недостаточно товара: " + product.getName());
        var item = cart.getItems().stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst().orElse(null);
        if (item == null) {
            if (cart.getItems().size() >= 100) throw ApiException.conflict("В корзине не может быть больше 100 товаров");
            item = new CartItem(); item.setCart(cart); item.setProduct(product); cart.getItems().add(item);
        }
        item.setQuantity(quantity);
        return CartResponse.from(cart);
    }
    public void remove(String email, Long productId) {
        var cart = lockedCart(email);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
    }
    public void clear(String email) { lockedCart(email).getItems().clear(); }
    public OrderResponse checkout(String email) {
        var cart = lockedCart(email);
        var request = new CreateOrderRequest(cart.getItems().stream()
            .map(i -> new OrderItemRequest(i.getProduct().getId(), i.getQuantity())).toList());
        var order = orders.createOrder(email, request);
        cart.getItems().clear();
        return order;
    }
    private Cart lockedCart(String email) {
        // Lock the user even when the cart does not exist yet, serializing first creation and checkout.
        var user = users.findLockedByEmail(email).orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        return carts.findByUserId(user.getId()).orElseGet(() -> {
            var cart = new Cart(); cart.setUser(user); return carts.save(cart);
        });
    }
}
