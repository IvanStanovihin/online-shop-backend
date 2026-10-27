package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(@NotBlank @Size(max = 255) String name, @Size(max = 10000) String description, @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) java.math.BigDecimal price, @NotNull @Min(0) Integer stockQuantity, @NotNull @Positive Long categoryId) {}
