package org.online.shop.magnit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank @Size(max = 255)
    private String name;

    @Size(max = 10000)
    private String description;

    @Column(nullable = false)
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @Min(0)
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}