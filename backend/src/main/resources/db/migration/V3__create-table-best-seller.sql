CREATE TABLE IF NOT EXISTS best_seller(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

INSERT INTO best_seller (product_id)
SELECT id FROM product
ON CONFLICT DO NOTHING;