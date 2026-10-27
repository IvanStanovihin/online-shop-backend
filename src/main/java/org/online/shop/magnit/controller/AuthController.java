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
@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Регистрация покупателя и вход с JWT")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Зарегистрировать покупателя", description = "Создаёт пользователя с ролью ROLE_USER; пароль хранится как BCrypt-хэш. 409 при занятом email.")
    public void register(@Valid @RequestBody RegisterRequest r) { authService.register(r); }
    @PostMapping("/login")
    @Operation(summary = "Получить JWT", description = "Передавайте token в заголовке Authorization: Bearer <token>.")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return new AuthResponse(authService.login(r)); }
}
