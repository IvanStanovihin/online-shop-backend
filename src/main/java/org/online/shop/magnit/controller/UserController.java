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
@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
@Tag(name = "Профиль") @SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService users;
    @GetMapping("/me")
    @Operation(summary = "Мой профиль и роли", description = "Используйте роли для отображения разделов фронтенда; сервер проверяет доступ независимо.")
    public UserResponse me(Authentication a) { return users.me(a.getName()); }
}
