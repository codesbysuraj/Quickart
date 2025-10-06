-- QuickKart Database Schema
-- Run this script in MySQL Workbench or command line

CREATE DATABASE IF NOT EXISTS quickkart_db;
USE quickkart_db;

-- Users table (for both customers and vendors)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(15),
    address VARCHAR(255)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    description TEXT,
    image_url TEXT,  -- Changed from VARCHAR(500) to TEXT to support base64 images
    stock INT DEFAULT 0,
    vendor_id BIGINT,
    FOREIGN KEY (vendor_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Cart table
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PLACED',
    total_amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================================
-- INSERT TEST DATA
-- ============================================================================

-- Test Users (2 Vendors + 2 Customers)
INSERT INTO users (username, password, role, pincode, email, phone, address) VALUES
-- VENDORS
('vendor1', 'vendor123', 'VENDOR', '400607', 'vendor1@quickkart.com', '9876543210', 'Shop 1, Market Road, Mumbai'),
('vendor2', 'vendor123', 'VENDOR', '400607', 'vendor2@quickkart.com', '9876543211', 'Shop 2, Main Street, Mumbai'),
('vendor_pune', 'vendor123', 'VENDOR', '411001', 'vendor3@quickkart.com', '9876543212', 'Shop 3, FC Road, Pune'),

-- CUSTOMERS
('customer1', 'customer123', 'CUSTOMER', '400607', 'customer1@gmail.com', '9123456789', 'Flat 101, Building A, Mumbai'),
('customer2', 'customer123', 'CUSTOMER', '400607', 'customer2@gmail.com', '9123456790', 'Flat 202, Building B, Mumbai'),
('customer_pune', 'customer123', 'CUSTOMER', '411001', 'customer3@gmail.com', '9123456791', 'Flat 303, Pune');

-- Test Products for Pincode 400607 (Mumbai)

-- Vendor 1 Products (Grocery Store)
INSERT INTO products (name, category, price, pincode, description, image_url, stock, vendor_id) VALUES
('Fresh Apples', 'Fruits', 150.00, '400607', 'Fresh red apples from local farm, 1kg pack', 'https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=400', 50, 1),
('Bananas', 'Fruits', 40.00, '400607', 'Fresh yellow bananas, dozen', 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400', 100, 1),
('Tomatoes', 'Vegetables', 30.00, '400607', 'Fresh tomatoes, 1kg', 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400', 80, 1),
('Onions', 'Vegetables', 35.00, '400607', 'Fresh onions, 1kg', 'https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=400', 90, 1),
('Bread', 'Bakery', 45.00, '400607', 'Whole wheat bread loaf', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400', 30, 1),
('Milk', 'Dairy', 60.00, '400607', 'Fresh cow milk, 1 liter', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400', 40, 1),
('Eggs', 'Dairy', 70.00, '400607', 'Fresh eggs, 12 pieces', 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400', 60, 1),
('Rice', 'Grains', 180.00, '400607', 'Basmati rice, 5kg pack', 'https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400', 25, 1);

-- Vendor 2 Products (Electronics & Fashion)
INSERT INTO products (name, category, price, pincode, description, image_url, stock, vendor_id) VALUES
('Wireless Mouse', 'Electronics', 599.00, '400607', 'Ergonomic wireless mouse with USB receiver', 'https://images.unsplash.com/photo-1527814050087-3793815479db?w=400', 20, 2),
('USB Cable', 'Electronics', 199.00, '400607', 'Type-C USB charging cable, 1.5m', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=400', 50, 2),
('Headphones', 'Electronics', 1299.00, '400607', 'Over-ear wired headphones with mic', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400', 15, 2),
('T-Shirt Men', 'Fashion', 499.00, '400607', 'Cotton round neck t-shirt, multiple colors', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400', 30, 2),
('Jeans', 'Fashion', 1299.00, '400607', 'Blue denim jeans, slim fit', 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400', 25, 2),
('Notebook', 'Stationery', 80.00, '400607', 'A4 ruled notebook, 200 pages', 'https://images.unsplash.com/photo-1544816155-12df9643f363?w=400', 100, 2),
('Pen Set', 'Stationery', 150.00, '400607', 'Blue/black ballpoint pens, 10 pack', 'https://images.unsplash.com/photo-1586895347513-f67a6e12d6f6?w=400', 80, 2),
('Water Bottle', 'Accessories', 299.00, '400607', 'Stainless steel water bottle, 1 liter', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400', 40, 2);

-- Test Products for Pincode 411001 (Pune)
INSERT INTO products (name, category, price, pincode, description, image_url, stock, vendor_id) VALUES
('Mangoes', 'Fruits', 200.00, '411001', 'Alphonso mangoes, premium quality, 1kg', 'https://images.unsplash.com/photo-1605027990121-cbae9d3ce9bd?w=400', 30, 3),
('Laptop Bag', 'Accessories', 899.00, '411001', 'Water-resistant laptop backpack, 15.6 inch', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400', 15, 3),
('Coffee Beans', 'Beverages', 599.00, '411001', 'Arabica coffee beans, 250g pack', 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400', 20, 3);

-- Sample Cart Items (for testing - customer1 has items in cart)
INSERT INTO cart (user_id, product_id, quantity) VALUES
(4, 1, 2),  -- customer1 has 2 Fresh Apples
(4, 9, 1),  -- customer1 has 1 Wireless Mouse
(5, 3, 3);  -- customer2 has 3 Tomatoes

-- Sample Order History (for testing - customer1 has previous orders)
INSERT INTO orders (user_id, status, total_amount, created_at) VALUES
(4, 'DELIVERED', 890.00, '2025-09-25 10:30:00'),
(4, 'PLACED', 450.00, '2025-10-01 15:45:00'),
(5, 'DELIVERED', 1200.00, '2025-09-28 14:20:00');

-- Sample Order Items
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
-- Order 1 items (customer1's delivered order)
(1, 1, 3, 150.00),  -- 3 Fresh Apples
(1, 9, 1, 599.00),  -- 1 Wireless Mouse

-- Order 2 items (customer1's recent order)
(2, 5, 5, 45.00),   -- 5 Bread
(2, 6, 2, 60.00),   -- 2 Milk

-- Order 3 items (customer2's delivered order)
(3, 11, 1, 1299.00); -- 1 Headphones

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check all data
SELECT 'USERS' as TableName, COUNT(*) as Count FROM users
UNION ALL
SELECT 'PRODUCTS', COUNT(*) FROM products
UNION ALL
SELECT 'CART ITEMS', COUNT(*) FROM cart
UNION ALL
SELECT 'ORDERS', COUNT(*) FROM orders
UNION ALL
SELECT 'ORDER ITEMS', COUNT(*) FROM order_items;

-- Show test credentials
SELECT 
    '=== TEST CREDENTIALS ===' as Info,
    '' as Username,
    '' as Password,
    '' as Role
UNION ALL
SELECT 
    'VENDORS:',
    '',
    '',
    ''
UNION ALL
SELECT 
    '',
    username,
    password,
    role
FROM users WHERE role = 'VENDOR'
UNION ALL
SELECT 
    'CUSTOMERS:',
    '',
    '',
    ''
UNION ALL
SELECT 
    '',
    username,
    password,
    role
FROM users WHERE role = 'CUSTOMER';

-- Show products by pincode
SELECT 
    pincode,
    COUNT(*) as product_count,
    GROUP_CONCAT(DISTINCT category) as categories
FROM products
GROUP BY pincode;

-- ============================================================================
-- QUICKKART DATABASE SETUP COMPLETE!
-- ============================================================================
