CREATE TABLE IF NOT EXISTS products(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(15) NOT NULL UNIQUE,
    image_url TEXT NOT NULL,
    description TEXT NOT NULL,
    price REAL NOT NULL
);