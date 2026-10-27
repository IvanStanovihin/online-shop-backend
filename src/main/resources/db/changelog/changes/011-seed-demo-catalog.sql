--liquibase formatted sql

--changeset student:011-seed-demo-catalog
--comment: Demo catalog without fixed IDs or overwriting existing products.
INSERT INTO categories (name)
SELECT 'Молочные продукты' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Молочные продукты');

INSERT INTO categories (name)
SELECT 'Хлеб и выпечка' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Хлеб и выпечка');

INSERT INTO categories (name)
SELECT 'Напитки' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Напитки');

INSERT INTO categories (name)
SELECT 'Овощи и фрукты' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Овощи и фрукты');

INSERT INTO categories (name)
SELECT 'Бакалея' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Бакалея');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Молоко 3,2%, 1 л', 'Пастеризованное молоко в тетрапаке', 89.90, 120, c.id
FROM categories c
WHERE c.name = 'Молочные продукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Молоко 3,2%, 1 л');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Кефир 2,5%, 0,9 л', 'Классический кефир', 79.00, 85, c.id
FROM categories c
WHERE c.name = 'Молочные продукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Кефир 2,5%, 0,9 л');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Сыр «Российский», 200 г', 'Полутвёрдый сыр, нарезка', 219.50, 40, c.id
FROM categories c
WHERE c.name = 'Молочные продукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Сыр «Российский», 200 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Хлеб «Бородинский», 400 г', 'Ржано-пшеничный хлеб с кориандром', 55.00, 200, c.id
FROM categories c
WHERE c.name = 'Хлеб и выпечка'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Хлеб «Бородинский», 400 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Батон нарезной, 400 г', 'Пшеничный батон', 45.00, 150, c.id
FROM categories c
WHERE c.name = 'Хлеб и выпечка'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Батон нарезной, 400 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Круассан с шоколадом, 80 г', 'Сдобный круассан', 69.00, 60, c.id
FROM categories c
WHERE c.name = 'Хлеб и выпечка'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Круассан с шоколадом, 80 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Вода питьевая, 1,5 л', 'Негазированная питьевая вода', 39.00, 300, c.id
FROM categories c
WHERE c.name = 'Напитки'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Вода питьевая, 1,5 л');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Сок апельсиновый, 1 л', '100% сок, без добавленного сахара', 129.00, 75, c.id
FROM categories c
WHERE c.name = 'Напитки'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Сок апельсиновый, 1 л');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Чай чёрный, 25 пакетиков', 'Крепкий цейлонский чай', 149.00, 90, c.id
FROM categories c
WHERE c.name = 'Напитки'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Чай чёрный, 25 пакетиков');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Яблоки красные, 1 кг', 'Сочные яблоки для перекуса и выпечки', 129.90, 70, c.id
FROM categories c
WHERE c.name = 'Овощи и фрукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Яблоки красные, 1 кг');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Апельсины, 1 кг', 'Свежие цитрусовые', 149.90, 65, c.id
FROM categories c
WHERE c.name = 'Овощи и фрукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Апельсины, 1 кг');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Томаты, 500 г', 'Спелые томаты для салатов', 119.90, 50, c.id
FROM categories c
WHERE c.name = 'Овощи и фрукты'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Томаты, 500 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Рис круглозёрный, 800 г', 'Для каш, гарниров и домашних блюд', 99.90, 80, c.id
FROM categories c
WHERE c.name = 'Бакалея'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Рис круглозёрный, 800 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Гречневая крупа, 800 г', 'Гречневая ядрица', 89.90, 100, c.id
FROM categories c
WHERE c.name = 'Бакалея'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Гречневая крупа, 800 г');

INSERT INTO products (name, description, price, stock_quantity, category_id)
SELECT 'Макароны спирали, 450 г', 'Из твёрдых сортов пшеницы', 74.90, 120, c.id
FROM categories c
WHERE c.name = 'Бакалея'
  AND NOT EXISTS (SELECT 1 FROM products WHERE name = 'Макароны спирали, 450 г');
