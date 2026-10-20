ALTER TABLE cart ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0);

-- one row per (user, product): the oldest row keeps the count, the others go
UPDATE cart c
SET quantity = g.qty
FROM (SELECT MIN(id::text) AS keep_id, COUNT(*) AS qty FROM cart GROUP BY user_id, product_id) g
WHERE c.id::text = g.keep_id;

DELETE FROM cart
WHERE id::text NOT IN (SELECT MIN(id::text) FROM cart GROUP BY user_id, product_id);

ALTER TABLE cart ADD CONSTRAINT cart_user_product_unique UNIQUE (user_id, product_id);
