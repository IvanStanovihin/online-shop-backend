package org.online.shop.magnit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
@Entity @Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
@Getter @Setter @NoArgsConstructor
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false) private Cart cart;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) private Product product;
    @NotNull @Min(1) @Max(10000) @Column(nullable = false) private Integer quantity;
}
