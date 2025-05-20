-- Створення таблиці ролей
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Створення таблиці користувачів
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Зв'язкова таблиця для користувачів і ролей (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Таблиця співробітників
CREATE TABLE IF NOT EXISTS employees (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email_id VARCHAR(100) UNIQUE NOT NULL
);

-- Вставка ролей
INSERT INTO roles (name) VALUES
('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name) VALUES
('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

-- Вставка користувачів
INSERT INTO users (email, password) VALUES
('booking.bogdan@gmail.com', 'admin')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password) VALUES
('user@example.com', 'user123')
ON CONFLICT (email) DO NOTHING;

-- Призначення ролей користувачам
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'booking.bogdan@gmail.com' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'user@example.com' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- Вставка даних в employees (приклади з твого списку)
INSERT INTO employees (first_name, last_name, email_id) VALUES
('Artur', 'Hrihoriyan', 'artur@gmail.com'),
('Danylo', 'Budzhak', 'budzhak2004@gmail.com'),
('Іван', 'Шевчук', 'sheva@gmail.com'),
('Bohdan', 'Hrydko', 'booking.bogdan@gmail.com'),
('Alex', 'Slobozhenko', 'slobozh@gmail.com'),
('Kostya', 'Hanich', 'kostyakudo@gmail.com'),
('Diana', 'Bozhok', 'diana@gmail.com'),
('Artem', 'Rydyk', 'Rudyk1906@gmail.com'),
('test', 'test', 'test@gmail.com'),
('Miroslav', 'Teryohin', 'miradiv2004@gmail.com'),
('Gleb', 'Ostapov', 'Ostapov@gmail.com'),
('Volodymyr', 'Yatsenko', 'yatsenko@gmail.com'),
('Thomas', 'Noire', 'thomasnoire@gmail.com'),
('Іван', 'Петренко', 'ivan.petrenko@example.com'),
('Andrey', 'Kyhaivskiy', 'kyhaivskiyandrew@gmail.com'),
('Ernest', 'Pavlyk', 'pavlykernest1@gmail.com'),
('Yevheniy', 'Turchinckiy', 'turchinskiy22@gmail.com')
ON CONFLICT (email_id) DO NOTHING;
