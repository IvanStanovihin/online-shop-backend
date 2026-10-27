package org.online.shop.magnit.dto;

import org.online.shop.magnit.model.Cart;
import java.math.BigDecimal;
import java.util.List;
public record CartResponse(List<Item> items, BigDecimal total) {
    public record Item(Long productId, String productName, Integer quantity, Integer stockQuantity,
                       BigDecimal price, BigDecimal subtotal) {}
    public static CartResponse from(Cart cart) {
        var items = cart.getItems().stream().map(i -> new Item(i.getProduct().getId(), i.getProduct().getName(),
            i.getQuantity(), i.getProduct().getStockQuantity(), i.getProduct().getPrice(),
            i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();
        return new CartResponse(items, items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
