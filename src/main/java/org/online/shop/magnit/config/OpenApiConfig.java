package org.online.shop.magnit.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
    @Bean OpenAPI shopOpenApi() {
        return new OpenAPI().info(new Info().title("Магнит — учебный интернет-магазин").version("1.0")
            .description("Каталог доступен без входа. Получите token через /api/auth/login. " +
                "В Authorize вставьте токен без префикса Bearer. USER — покупки, MANAGER — обработка заказов, " +
                "ADMIN — каталог, заказы и роли. Ошибки: 400 — данные, 401 — вход, 403 — права, 404 — запись, 409 — конфликт."))
            .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
