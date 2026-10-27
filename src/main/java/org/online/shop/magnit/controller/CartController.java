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
@RestController @RequestMapping("/api/cart") @RequiredArgsConstructor
@Tag(name = "Корзина") @SecurityRequirement(name = "bearerAuth")
public class CartController {
    private final CartService carts;
    @GetMapping
    @Operation(summary = "Получить свою корзину", description = "Текущие цены, доступные остатки и итог. Товар до оформления не резервируется.")
    public CartResponse get(Authentication a) { return carts.get(a.getName()); }
    @PutMapping("/items/{productId}")
    @Operation(summary = "Задать количество товара", description = "Добавляет товар или заменяет его количество (1–10000). Не увеличивает повторно при повторе запроса.")
    public CartResponse put(@PathVariable @Positive Long productId, @Valid @RequestBody CartItemRequest r, Authentication a) { return carts.put(a.getName(), productId, r.quantity()); }
    @DeleteMapping("/items/{productId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить товар из корзины", description = "Отсутствующая позиция не вызывает ошибку.")
    public void remove(@PathVariable @Positive Long productId, Authentication a) { carts.remove(a.getName(), productId); }
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Очистить корзину", description = "Удаляет все позиции текущего пользователя.")
    public void clear(Authentication a) { carts.clear(a.getName()); }
    @PostMapping("/checkout") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оформить заказ из корзины", description = "Повторно проверяет остатки, создаёт заказ и очищает корзину в одной транзакции. При ошибке корзина сохраняется.")
    public OrderResponse checkout(Authentication a) { return carts.checkout(a.getName()); }
}
