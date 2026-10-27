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
@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
@Tag(name = "Категории")
public class CategoryController {
    private final CategoryService categories;
    @GetMapping
    @Operation(summary = "Найти категории", description = "Поиск по части названия name. Для формы товара используйте name как подпись, id как значение.")
    public List<Category> list(@RequestParam(required = false) @Size(max = 100) String name) { return categories.findAll(name); }
    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию по ID", description = "404, если категория отсутствует.")
    public Category get(@PathVariable @Positive Long id) { return categories.findById(id); }
}
