package org.online.shop.magnit.dto;

import org.online.shop.magnit.model.Product;
import java.math.BigDecimal;
public record ProductResponse(Long id, String name, String description, BigDecimal price,
                              Integer stockQuantity, Long categoryId, String categoryName) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
            p.getStockQuantity(), p.getCategory() == null ? null : p.getCategory().getId(),
            p.getCategory() == null ? null : p.getCategory().getName());
    }
}
