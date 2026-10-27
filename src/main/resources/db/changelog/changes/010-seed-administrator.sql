--liquibase formatted sql

--changeset student:010-seed-administrator
--comment: Initial administrator for the educational project; existing credentials are preserved.
INSERT INTO users (email, password, full_name)
SELECT 'admin@gmail.com', '$2a$10$iaqS0KT2npyGnQVLP1koZeNmEGezQaBvbicj59gEg04DAjVr6U1mC', 'Администратор'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u CROSS JOIN roles r
WHERE u.email = 'admin@gmail.com'
  AND r.name IN ('ROLE_USER', 'ROLE_ADMIN')
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
