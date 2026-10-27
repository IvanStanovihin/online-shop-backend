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
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor
@Tag(name = "Администратор") @SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final ProductService products;
    private final CategoryService categories;
    private final OrderService orders;
    private final UserService users;
    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать товар", description = "Требуется существующий categoryId; цены в рублях, остаток неотрицательный.")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest r) { return products.create(r); }
    @PutMapping("/products/{id}")
    @Operation(summary = "Изменить товар", description = "Полная замена редактируемых полей. Цена уже оформленных заказов сохраняется.")
    public ProductResponse updateProduct(@PathVariable @Positive Long id, @Valid @RequestBody ProductRequest r) { return products.update(id, r); }
    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить товар", description = "Товар из заказа или корзины удалить нельзя: 409. Для прекращения продаж задайте остаток 0.")
    public void deleteProduct(@PathVariable @Positive Long id) { products.delete(id); }
    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать категорию", description = "Название обязательно и уникально.")
    public Category createCategory(@Valid @RequestBody CategoryRequest r) { return categories.create(r); }
    @PutMapping("/categories/{id}")
    @Operation(summary = "Переименовать категорию", description = "Связи с товарами сохраняются.")
    public Category updateCategory(@PathVariable @Positive Long id, @Valid @RequestBody CategoryRequest r) { return categories.update(id, r); }
    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить категорию", description = "Существующие товары сохраняются и остаются без категории.")
    public void deleteCategory(@PathVariable @Positive Long id) { categories.delete(id); }
    @GetMapping("/orders")
    @Operation(summary = "Получить все заказы", description = "Просмотр заказов покупателей для обработки.")
    public List<OrderResponse> orders() { return orders.getAll(); }
    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Изменить статус заказа", description = "NEW → PAID → SHIPPED → DELIVERED; отмена из NEW или PAID. PAID — учебное подтверждение, платёжного шлюза нет.")
    public OrderResponse status(@PathVariable @Positive Long id, @RequestParam Order.OrderStatus status) { return orders.updateStatus(id, status); }
    @GetMapping("/users")
    @Operation(summary = "Список пользователей", description = "Данные для страницы управления ролями; пароли не возвращаются.")
    public List<UserResponse> users() { return users.list(); }
    @GetMapping("/roles")
    @Operation(summary = "Список доступных ролей", description = "ROLE_USER — покупатель, ROLE_MANAGER — обработка заказов, ROLE_ADMIN — полное управление.")
    public List<Role> roles() { return users.roles(); }
    @PutMapping("/users/{id}/roles")
    @Operation(summary = "Заменить роли пользователя", description = "Непустой набор имён ролей. Нельзя снять ADMIN у себя. Права проверяются по БД на каждом запросе.")
    public UserResponse roles(@PathVariable @Positive Long id, @Valid @RequestBody UpdateRolesRequest r, Authentication a) { return users.updateRoles(id, r, a.getName()); }
}
