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
@RestController @RequestMapping("/api/products") @RequiredArgsConstructor
@Tag(name = "Каталог")
public class ProductController {
    private final ProductService products;
    @GetMapping
    @Operation(summary = "Найти товары", description = "Необязательные фильтры: categoryId и часть названия name без учёта регистра.")
    public List<ProductResponse> list(@RequestParam(required = false) @Positive Long categoryId,
                                      @RequestParam(required = false) @Size(max = 255) String name) {
        return products.findAll(categoryId, name);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Получить товар по ID", description = "Возвращает товар с названием категории; 404, если товар отсутствует.")
    public ProductResponse get(@PathVariable @Positive Long id) { return products.findById(id); }
}
