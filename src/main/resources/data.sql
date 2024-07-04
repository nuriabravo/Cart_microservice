-- Create the 'cart' table
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    updated_at TIMESTAMP
);

-- Create the 'cart_products' table
CREATE TABLE IF NOT EXISTS cart_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    product_id BIGINT,
    product_name VARCHAR(255),
    product_description VARCHAR(255),
    quantity INT,
    price DECIMAL(10, 2),
    FOREIGN KEY (cart_id) REFERENCES cart(id)
);

-- Insert data into 'cart'
INSERT INTO cart (user_id, updated_at) VALUES (1, '2024-05-01 12:00:00');
INSERT INTO cart (user_id, updated_at) VALUES (2, '2024-05-02 12:00:00');
INSERT INTO cart (user_id, updated_at) VALUES (3, '2024-05-03 12:00:00');

-- Insert data into 'cart_products'
INSERT INTO cart_products (cart_id, product_id, product_name, product_description, quantity, price) VALUES
(1, 1, 'Jacket',  'Something indicate large central measure watch provide.', 1, 58.79),
(1, 2, 'Building Blocks',  'Agent word occur number chair.', 2, 7.89),
(2, 3, 'Swimming Goggles',  'Walk range media doctor interest.', 1, 30.53),
(3, 4, 'Football', 'Country expect price certain different bag everyone.', 1, 21.93);
