package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;

public record CartItemRequest(@NotNull @Min(1) @Max(10000) Integer quantity) {}
