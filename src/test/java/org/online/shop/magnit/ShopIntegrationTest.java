package org.online.shop.magnit;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.online.shop.magnit.dto.*;
import org.online.shop.magnit.model.*;
import org.online.shop.magnit.repository.*;
import org.online.shop.magnit.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ShopIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired ProductRepository products;
    @Autowired CategoryRepository categories;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired OrderService orders;
    @Autowired CartService carts;
    @Autowired UserService userService;
    Product milk;
    Category dairy;
    User buyer, other, admin, manager;
    String buyerToken, adminToken, managerToken;

    @BeforeEach void setup() throws Exception {
        // Refuse to clear a non-test database when datasource properties are overridden.
        try (var connection = jdbc.getDataSource().getConnection()) {
            String url = connection.getMetaData().getURL();
            assertThat(url.startsWith("jdbc:h2:mem:shop") ||
                url.matches("jdbc:postgresql://(localhost|127[.]0[.]0[.]1):[0-9]+/shop_test(_[a-z]+)?"))
                .as("Tests require H2 memory or a local database named shop_test[_suffix]").isTrue();
        }
        for (String table : List.of("cart_items", "carts", "order_items", "orders", "products", "categories", "user_roles", "users"))
            jdbc.update("DELETE FROM " + table);
        buyer = user("buyer@example.com", "ROLE_USER");
        other = user("other@example.com", "ROLE_USER");
        admin = user("admin@example.com", "ROLE_ADMIN");
        manager = user("manager@example.com", "ROLE_MANAGER");
        buyerToken = jwt.generateToken(buyer); adminToken = jwt.generateToken(admin); managerToken = jwt.generateToken(manager);
        dairy = new Category(); dairy.setName("Молочные продукты"); dairy = categories.save(dairy);
        milk = product("Молоко", 10);
    }
    User user(String email, String role) {
        var u = new User(); u.setEmail(email); u.setFullName("Тест"); u.setPassword(encoder.encode("Password123!"));
        u.getRoles().add(roles.findByName(role).orElseThrow()); return users.save(u);
    }
    Product product(String name, int stock) {
        var p = new Product(); p.setName(name); p.setPrice(new BigDecimal("89.90")); p.setStockQuantity(stock);
        p.setCategory(dairy); return products.save(p);
    }
    MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }
    MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, Object value) throws Exception {
        return request.contentType("application/json").content(json.writeValueAsString(value));
    }
    CreateOrderRequest request(int qty) { return new CreateOrderRequest(List.of(new OrderItemRequest(milk.getId(), qty))); }
    long createOrder(int qty) throws Exception {
        var result = mvc.perform(body(auth(post("/api/orders"), buyerToken), request(qty)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(89.90 * qty))
            .andExpect(jsonPath("$.user").doesNotExist()).andExpect(jsonPath("$.items[0].order").doesNotExist()).andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test void migrationsCreateNineTablesAndThreeRoles() {
        for (String table : List.of("users", "roles", "user_roles", "categories", "products", "orders", "order_items", "carts", "cart_items"))
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class)).isNotNull();
        assertThat(roles.findAll()).extracting(Role::getName).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER");
    }
    @Test void registerLoginAndDuplicateEmail() throws Exception {
        var registration = new RegisterRequest("new@example.com", "Password123!", "Новый покупатель");
        mvc.perform(body(post("/api/auth/register"), registration)).andExpect(status().isCreated());
        var saved = users.findByEmail(registration.email()).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo(registration.password());
        assertThat(encoder.matches(registration.password(), saved.getPassword())).isTrue();
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly("ROLE_USER");
        mvc.perform(body(post("/api/auth/register"), registration)).andExpect(status().isConflict());
        var result = mvc.perform(body(post("/api/auth/login"), new LoginRequest(registration.email(), registration.password())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString()).andReturn();
        String token = json.readTree(result.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(auth(get("/api/users/me"), token)).andExpect(status().isOk())
            .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.email").value(registration.email()));
        mvc.perform(body(post("/api/auth/login"), new LoginRequest(registration.email(), "wrong")))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }
    @Test void validatesFieldsAndNestedItems() throws Exception {
        mvc.perform(body(post("/api/auth/register"), new RegisterRequest("bad", "short", "")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists()).andExpect(jsonPath("$.errors.fullName").exists());
        mvc.perform(body(auth(post("/api/orders"), buyerToken), request(-1)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors['items[0].quantity']").exists());
        mvc.perform(auth(post("/api/orders"), buyerToken).contentType("application/json").content("{\"items\":[null]}"))
            .andExpect(status().isBadRequest());
        mvc.perform(body(auth(post("/api/admin/products"), adminToken),
            new ProductRequest("", "", new BigDecimal("-1"), -1, null))).andExpect(status().isBadRequest());
        mvc.perform(auth(post("/api/orders"), buyerToken).contentType("application/json").content("{broken"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/no-id")).andExpect(status().isBadRequest());
    }
    @Test void searchesAndSerializesCategoryWithoutLazyErrors() throws Exception {
        mvc.perform(get("/api/products").param("name", "мОЛ").param("categoryId", dairy.getId().toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].categoryName").value("Молочные продукты"))
            .andExpect(jsonPath("$[0].id").value(milk.getId()));
        mvc.perform(get("/api/products").param("name", "missing")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/categories").param("name", "мол")).andExpect(jsonPath("$[0].id").value(dairy.getId()));
        mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    }
    @Test void rejectsMissingMalformedExpiredTokensAndInsufficientRole() throws Exception {
        mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(auth(get("/api/orders"), "broken")).andExpect(status().isUnauthorized());
        String expired = io.jsonwebtoken.Jwts.builder().subject(buyer.getEmail())
            .expiration(new Date(System.currentTimeMillis() - 10000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("test-only-secret-at-least-thirty-two-bytes-long".getBytes(java.nio.charset.StandardCharsets.UTF_8))).compact();
        mvc.perform(auth(get("/api/orders"), expired)).andExpect(status().isUnauthorized());
        mvc.perform(auth(get("/api/admin/users"), buyerToken)).andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(auth(get("/api/admin/users"), managerToken)).andExpect(status().isForbidden());
        mvc.perform(auth(get("/api/management/orders"), managerToken)).andExpect(status().isOk());
    }
    @Test void orderOwnershipAndCancellationRestoreStockOnce() throws Exception {
        long id = createOrder(2);
        assertThat(products.findById(milk.getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
        mvc.perform(auth(get("/api/orders/" + id), jwt.generateToken(other))).andExpect(status().isNotFound());
        mvc.perform(auth(post("/api/orders/" + id + "/cancel"), jwt.generateToken(other))).andExpect(status().isNotFound());
        mvc.perform(auth(get("/api/orders/" + id), buyerToken)).andExpect(status().isOk());
        mvc.perform(auth(get("/api/orders"), buyerToken)).andExpect(status().isOk()).andExpect(jsonPath("$[0].items[0].productName").value("Молоко"));
        for (int i = 0; i < 2; i++) mvc.perform(auth(post("/api/orders/" + id + "/cancel"), buyerToken)).andExpect(status().isOk());
        assertThat(products.findById(milk.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
        mvc.perform(auth(patch("/api/management/orders/" + id + "/status").param("status", "PAID"), managerToken))
            .andExpect(status().isConflict());
    }
    @Test void rollsBackWholeOrderOnInsufficientStock() throws Exception {
        var unavailable = product("Нет в наличии", 0);
        var req = new CreateOrderRequest(List.of(new OrderItemRequest(milk.getId(), 3), new OrderItemRequest(unavailable.getId(), 1)));
        mvc.perform(body(auth(post("/api/orders"), buyerToken), req)).andExpect(status().isConflict());
        assertThat(products.findById(milk.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isZero();
    }
    @Test void duplicateProductsAreAggregatedAndPriceIsFrozen() throws Exception {
        var req = new CreateOrderRequest(List.of(new OrderItemRequest(milk.getId(), 2), new OrderItemRequest(milk.getId(), 3)));
        var result = mvc.perform(body(auth(post("/api/orders"), buyerToken), req)).andExpect(status().isCreated())
            .andExpect(jsonPath("$.items.length()").value(1)).andExpect(jsonPath("$.items[0].quantity").value(5)).andReturn();
        long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        jdbc.update("UPDATE products SET price = 100 WHERE id = ?", milk.getId());
        mvc.perform(auth(get("/api/orders/" + id), buyerToken)).andExpect(jsonPath("$.total").value(449.50));
    }
    @Test void managerCanProcessButNotSkipStatus() throws Exception {
        long id = createOrder(1);
        mvc.perform(auth(patch("/api/management/orders/" + id + "/status").param("status", "DELIVERED"), managerToken)).andExpect(status().isConflict());
        for (var status : List.of("PAID", "SHIPPED", "DELIVERED"))
            mvc.perform(auth(patch("/api/management/orders/" + id + "/status").param("status", status), managerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(status));
        mvc.perform(auth(post("/api/orders/" + id + "/cancel"), buyerToken)).andExpect(status().isConflict());
    }
    @Test void cartCheckoutIsAtomicAndIsolated() throws Exception {
        mvc.perform(auth(get("/api/cart"), buyerToken)).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(body(auth(put("/api/cart/items/" + milk.getId()), buyerToken), new CartItemRequest(2))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/cart"), jwt.generateToken(other))).andExpect(jsonPath("$.items.length()").value(0));
        jdbc.update("UPDATE products SET stock_quantity = 0 WHERE id = ?", milk.getId());
        mvc.perform(auth(post("/api/cart/checkout"), buyerToken)).andExpect(status().isConflict());
        mvc.perform(auth(get("/api/cart"), buyerToken)).andExpect(jsonPath("$.items[0].quantity").value(2));
        jdbc.update("UPDATE products SET stock_quantity = 10 WHERE id = ?", milk.getId());
        mvc.perform(auth(post("/api/cart/checkout"), buyerToken)).andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(179.80));
        mvc.perform(auth(get("/api/cart"), buyerToken)).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(auth(post("/api/cart/checkout"), buyerToken)).andExpect(status().isBadRequest());
        assertThat(products.findById(milk.getId()).orElseThrow().getStockQuantity()).isEqualTo(8);
    }
    @Test void roleChangesApplyToExistingJwt() throws Exception {
        mvc.perform(body(auth(put("/api/admin/users/" + buyer.getId() + "/roles"), adminToken),
            new UpdateRolesRequest(Set.of("ROLE_MANAGER")))).andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(auth(get("/api/management/orders"), buyerToken)).andExpect(status().isOk());
        mvc.perform(body(auth(put("/api/admin/users/" + buyer.getId() + "/roles"), adminToken),
            new UpdateRolesRequest(Set.of("ROLE_USER")))).andExpect(status().isOk());
        mvc.perform(auth(get("/api/management/orders"), buyerToken)).andExpect(status().isForbidden());
        mvc.perform(body(auth(put("/api/admin/users/" + admin.getId() + "/roles"), adminToken),
            new UpdateRolesRequest(Set.of("ROLE_USER")))).andExpect(status().isConflict());
        mvc.perform(body(auth(put("/api/admin/users/" + buyer.getId() + "/roles"), adminToken),
            Map.of("roles", List.of("ROLE_ROOT")))).andExpect(status().isBadRequest());
    }
    @Test void deletionRespectsReferencesAndMissingRecords() throws Exception {
        createOrder(1);
        mvc.perform(auth(delete("/api/admin/products/" + milk.getId()), adminToken)).andExpect(status().isConflict());
        mvc.perform(auth(delete("/api/admin/products/999999"), adminToken)).andExpect(status().isNotFound());
        mvc.perform(auth(delete("/api/admin/categories/" + dairy.getId()), adminToken)).andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + milk.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.categoryId").isEmpty());
    }
    @Test void swaggerDocumentsEveryOperationAndJwt() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer")).andReturn();
        var paths = json.readTree(result.getResponse().getContentAsString()).path("paths");
        assertThat(paths.size()).isGreaterThan(15);
        for (var path : paths) for (var operation : path) {
            assertThat(operation.path("summary").asText()).isNotBlank();
            assertThat(operation.path("description").asText()).isNotBlank();
        }
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(options("/api/orders").header("Origin", "http://localhost:5500")
            .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5500"));
    }
    @Test void catalogCrudAndCartEditing() throws Exception {
        var categoryResult = mvc.perform(body(auth(post("/api/admin/categories"), adminToken),
            new CategoryRequest("Бакалея"))).andExpect(status().isCreated()).andReturn();
        long categoryId = json.readTree(categoryResult.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(body(auth(put("/api/admin/categories/" + categoryId), adminToken),
            new CategoryRequest("Крупы"))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Крупы"));
        var productResult = mvc.perform(body(auth(post("/api/admin/products"), adminToken),
            new ProductRequest("Рис", "Крупа", new BigDecimal("100.00"), 5, categoryId)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.categoryName").value("Крупы")).andReturn();
        long productId = json.readTree(productResult.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(body(auth(put("/api/admin/products/" + productId), adminToken),
            new ProductRequest("Рис", "Крупа", new BigDecimal("110.00"), 6, categoryId))).andExpect(status().isOk());
        mvc.perform(body(auth(put("/api/cart/items/" + productId), buyerToken), new CartItemRequest(1))).andExpect(status().isOk());
        mvc.perform(body(auth(put("/api/cart/items/" + productId), buyerToken), new CartItemRequest(2)))
            .andExpect(jsonPath("$.items[0].quantity").value(2)).andExpect(jsonPath("$.total").value(220));
        mvc.perform(auth(delete("/api/cart/items/" + productId), buyerToken)).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/cart"), buyerToken)).andExpect(status().isNoContent());
        mvc.perform(auth(delete("/api/admin/products/" + productId), adminToken)).andExpect(status().isNoContent());
        mvc.perform(body(auth(post("/api/admin/categories"), adminToken), new CategoryRequest("Крупы"))).andExpect(status().isConflict());
    }
    @Test void unicodePasswordsRespectBcryptByteLimit() throws Exception {
        mvc.perform(body(post("/api/auth/register"), new RegisterRequest("unicode@example.com", "ПарольНадёжный123", "Тест")))
            .andExpect(status().isCreated());
        mvc.perform(body(post("/api/auth/login"), new LoginRequest("unicode@example.com", "ПарольНадёжный123")))
            .andExpect(status().isOk());
        mvc.perform(body(post("/api/auth/register"), new RegisterRequest("long@example.com", "Я".repeat(40), "Тест")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.password").exists());
    }
    @Test void concurrentRoleChangesKeepAnAdministrator() throws Exception {
        var secondAdmin = user("admin2@example.com", "ROLE_ADMIN");
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> demoteFirst = () -> {
                start.await();
                try { userService.updateRoles(admin.getId(), new UpdateRolesRequest(Set.of("ROLE_USER")), secondAdmin.getEmail()); return true; }
                catch (org.online.shop.magnit.exception.ApiException e) { return false; }
            };
            Callable<Boolean> demoteSecond = () -> {
                start.await();
                try { userService.updateRoles(secondAdmin.getId(), new UpdateRolesRequest(Set.of("ROLE_USER")), admin.getEmail()); return true; }
                catch (org.online.shop.magnit.exception.ApiException e) { return false; }
            };
            var first = pool.submit(demoteFirst); var second = pool.submit(demoteSecond); start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(users.countByRolesName("ROLE_ADMIN")).isEqualTo(1);
    }
    @Test void concurrentPurchasesCannotOversell() throws Exception {
        jdbc.update("UPDATE products SET stock_quantity = 1 WHERE id = ?", milk.getId());
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> buy = () -> {
                start.await();
                try { orders.createOrder(buyer.getEmail(), request(1)); return true; }
                catch (org.online.shop.magnit.exception.ApiException e) {
                    assertThat(e.getStatus().value()).isEqualTo(409); return false;
                }
            };
            var first = pool.submit(buy); var second = pool.submit(buy); start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(products.findById(milk.getId()).orElseThrow().getStockQuantity()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isEqualTo(1);
    }
    @Test void concurrentCheckoutCreatesOnlyOneOrder() throws Exception {
        carts.put(buyer.getEmail(), milk.getId(), 1);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> checkout = () -> {
                start.await();
                try { carts.checkout(buyer.getEmail()); return true; }
                catch (org.online.shop.magnit.exception.ApiException e) {
                    assertThat(e.getStatus().value()).isEqualTo(400); return false;
                }
            };
            var first = pool.submit(checkout); var second = pool.submit(checkout); start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class)).isEqualTo(1);
    }
}

