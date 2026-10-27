package org.online.shop.magnit;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeedMigrationTest {
    private Connection database() throws Exception {
        return DriverManager.getConnection("jdbc:h2:mem:seed_" + UUID.randomUUID()
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
    }
    private Liquibase migrations(Connection connection) throws Exception {
        return new Liquibase("db/changelog/db.changelog-master.yaml", new ClassLoaderResourceAccessor(),
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection)));
    }
    private void execute(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement()) { statement.execute(sql); }
    }
    private String value(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next(); return result.getString(1);
        }
    }
    @Test void freshDatabaseHasAdministratorAndThreeProductsInEveryCategory() throws Exception {
        try (var connection = database(); var migration = migrations(connection)) {
            migration.update(new Contexts(), new LabelExpression());
            String hash = value(connection, "SELECT password FROM users WHERE email='admin@gmail.com'");
            assertThat(hash).isNotEqualTo("zaq123456");
            assertThat(new BCryptPasswordEncoder().matches("zaq123456", hash)).isTrue();
            assertThat(value(connection, "SELECT count(*) FROM users")).isEqualTo("1");
            assertThat(value(connection, "SELECT count(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id JOIN users u ON u.id=ur.user_id WHERE u.email='admin@gmail.com' AND r.name IN ('ROLE_ADMIN','ROLE_USER')")).isEqualTo("2");
            assertThat(value(connection, "SELECT count(*) FROM categories")).isEqualTo("5");
            assertThat(value(connection, "SELECT count(*) FROM products")).isEqualTo("15");
            assertThat(value(connection, "SELECT count(*) FROM (SELECT category_id FROM products GROUP BY category_id HAVING count(*)=3) grouped")).isEqualTo("5");
            assertThat(value(connection, "SELECT count(*) FROM products WHERE price<=0 OR stock_quantity<=0 OR category_id IS NULL")).isEqualTo("0");
        }
    }
    @Test void restartDoesNotDuplicateOrResetChangedData() throws Exception {
        try (var connection = database(); var migration = migrations(connection)) {
            migration.update(new Contexts(), new LabelExpression());
            execute(connection, "UPDATE products SET price=12.34, stock_quantity=7 WHERE name='Молоко 3,2%, 1 л'");
            execute(connection, "UPDATE users SET password='changed-password-hash' WHERE email='admin@gmail.com'");
            migration.update(new Contexts(), new LabelExpression());
            assertThat(value(connection, "SELECT count(*) FROM users")).isEqualTo("1");
            assertThat(value(connection, "SELECT count(*) FROM products")).isEqualTo("15");
            assertThat(value(connection, "SELECT price FROM products WHERE name='Молоко 3,2%, 1 л'")).isEqualTo("12.34");
            assertThat(value(connection, "SELECT stock_quantity FROM products WHERE name='Молоко 3,2%, 1 л'")).isEqualTo("7");
            assertThat(value(connection, "SELECT password FROM users WHERE email='admin@gmail.com'")).isEqualTo("changed-password-hash");
        }
    }
    @Test void existingRecordsAndDifferentIdsArePreserved() throws Exception {
        try (var connection = database(); var migration = migrations(connection)) {
            // Only schema and roles, before the two new seed changesets.
            migration.update(8, new Contexts(), new LabelExpression());
            execute(connection, "INSERT INTO categories(name) VALUES('Собственная категория'),('Молочные продукты')");
            execute(connection, "INSERT INTO products(name,description,price,stock_quantity,category_id) SELECT 'Молоко 3,2%, 1 л','Существующее описание',77.00,5,id FROM categories WHERE name='Молочные продукты'");
            execute(connection, "INSERT INTO users(email,password,full_name) VALUES('admin@gmail.com','existing-password-hash','Существующее имя')");
            migration.update(new Contexts(), new LabelExpression());
            assertThat(value(connection, "SELECT count(*) FROM categories")).isEqualTo("6");
            assertThat(value(connection, "SELECT count(*) FROM products")).isEqualTo("15");
            assertThat(value(connection, "SELECT price FROM products WHERE name='Молоко 3,2%, 1 л'")).isEqualTo("77.00");
            assertThat(value(connection, "SELECT full_name FROM users WHERE email='admin@gmail.com'")).isEqualTo("Существующее имя");
            assertThat(value(connection, "SELECT password FROM users WHERE email='admin@gmail.com'")).isEqualTo("existing-password-hash");
            assertThat(value(connection, "SELECT count(*) FROM user_roles")).isEqualTo("2");
            // Auto-generated IDs are still usable after seeding.
            execute(connection, "INSERT INTO categories(name) VALUES('Новая категория после миграции')");
            assertThat(value(connection, "SELECT count(*) FROM categories")).isEqualTo("7");
        }
    }
}
