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
@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
@Tag(name = "Мои заказы") @SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orders;
    @GetMapping
    @Operation(summary = "История своих заказов", description = "Заказы текущего пользователя от новых к старым.")
    public List<OrderResponse> list(Authentication a) { return orders.getOrdersForUser(a.getName()); }
    @GetMapping("/{id}")
    @Operation(summary = "Получить свой заказ", description = "Для отсутствующего или чужого заказа возвращается 404.")
    public OrderResponse get(@PathVariable @Positive Long id, Authentication a) { return orders.getOrderForUser(a.getName(), id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оформить заказ", description = "Цена берётся из БД. Остатки списываются атомарно. Повторы товара суммируются; нехватка товара — 409.")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest r, Authentication a) { return orders.createOrder(a.getName(), r); }
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Отменить свой новый заказ", description = "Возвращает остатки ровно один раз. Заказы после NEW отменяет сотрудник.")
    public OrderResponse cancel(@PathVariable @Positive Long id, Authentication a) { return orders.cancel(a.getName(), id); }
}
