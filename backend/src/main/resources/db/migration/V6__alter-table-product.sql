ALTER TABLE product
    ALTER COLUMN name TYPE VARCHAR(60),
    ALTER COLUMN price TYPE NUMERIC(10, 2),
    ADD COLUMN category VARCHAR(20);

UPDATE product SET category = CASE WHEN name = 'Cake' THEN 'SNACK' ELSE 'HOT_COFFEE' END;

ALTER TABLE product
    ALTER COLUMN category SET NOT NULL,
    ADD CONSTRAINT product_category_check CHECK (category IN ('HOT_COFFEE', 'COLD_COFFEE', 'SNACK')),
    ADD CONSTRAINT product_price_positive CHECK (price > 0);
