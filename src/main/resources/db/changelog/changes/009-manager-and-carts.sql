--liquibase formatted sql

--changeset student:009-manager-and-carts
INSERT INTO roles (name) VALUES ('ROLE_MANAGER');

CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT uk_cart_items_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_cart_items_quantity CHECK (quantity BETWEEN 1 AND 10000)
);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

