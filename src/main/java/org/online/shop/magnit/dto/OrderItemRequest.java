package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;

public record OrderItemRequest(@NotNull @Positive Long productId, @NotNull @Min(1) @Max(10000) Integer quantity) {}
