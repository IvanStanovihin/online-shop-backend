package org.online.shop.magnit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.Category;
import org.online.shop.magnit.model.Role;
import org.online.shop.magnit.model.Order;
import org.online.shop.magnit.service.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/management/orders") @RequiredArgsConstructor
@Tag(name = "Обработка заказов") @SecurityRequirement(name = "bearerAuth")
public class ManagementController {
    private final OrderService orders;
    @GetMapping
    @Operation(summary = "Заказы для менеджера", description = "Доступно MANAGER и ADMIN.")
    public List<OrderResponse> list() { return orders.getAll(); }
    @PatchMapping("/{id}/status")
    @Operation(summary = "Обработать заказ", description = "NEW → PAID → SHIPPED → DELIVERED; отмена из NEW/PAID. Отмена возвращает остаток.")
    public OrderResponse status(@PathVariable @Positive Long id, @RequestParam Order.OrderStatus status) { return orders.updateStatus(id, status); }
}
