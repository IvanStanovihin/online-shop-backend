package org.online.shop.magnit.dto;

import jakarta.validation.constraints.*;

public record CreateOrderRequest(@NotEmpty @Size(max = 100) java.util.List<@NotNull @jakarta.validation.Valid OrderItemRequest> items) {}
