-- links each sold item to its product so best sellers can be counted (RF-CAT-6);
-- SET NULL keeps order history when a product is deleted
ALTER TABLE order_item ADD COLUMN product_id UUID;
ALTER TABLE order_item
    ADD CONSTRAINT order_item_product_fk FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE SET NULL;

-- orders placed before this migration only have the name snapshot
UPDATE order_item oi SET product_id = p.id FROM product p WHERE p.name = oi.product_name;

CREATE INDEX IF NOT EXISTS order_item_product_id_idx ON order_item (product_id);

-- the ranking is computed from orders now
DROP TABLE IF EXISTS best_seller;
