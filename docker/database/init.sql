-- Initialize test database schema

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    in_stock BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample test data
INSERT INTO users (username, email, password, first_name, last_name) VALUES
('testuser1', 'testuser1@example.com', 'password123', 'Test', 'User1'),
('testuser2', 'testuser2@example.com', 'password456', 'Test', 'User2'),
('admin', 'admin@example.com', 'admin123', 'Admin', 'User');

INSERT INTO products (name, description, price, category, in_stock) VALUES
('Laptop', 'High-performance laptop', 999.99, 'Electronics', true),
('Mouse', 'Wireless mouse', 29.99, 'Electronics', true),
('Keyboard', 'Mechanical keyboard', 79.99, 'Electronics', true),
('Monitor', '4K monitor', 299.99, 'Electronics', false);

INSERT INTO orders (user_id, total_amount, status) VALUES
(1, 1029.98, 'completed'),
(2, 79.99, 'pending'),
(1, 299.99, 'cancelled');
