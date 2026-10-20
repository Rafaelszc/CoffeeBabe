CREATE TABLE IF NOT EXISTS product(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(15) NOT NULL UNIQUE,
    image_url TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price REAL NOT NULL
);

INSERT INTO product (name, image_url, description, price)
VALUES
('Cappuccino', 'src/assets/product-cappuccino.jpg', 'A creamy espresso-based drink with steamed milk.', 4.0),
('Mocha', 'src/assets/product-mocha.jpg', 'A rich, chocolate-flavored coffee drink.', 4.5),
('Cake', 'src/assets/product-cake.jpg', 'A delicious, moist cake perfect for any occasion.', 3.75)
ON CONFLICT DO NOTHING;