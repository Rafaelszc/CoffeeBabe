CREATE TABLE IF NOT EXISTS product_image(
    product_id UUID PRIMARY KEY,
    content_type VARCHAR(20) NOT NULL,
    data BYTEA NOT NULL,
    etag VARCHAR(64) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
);

-- images now live in product_image and are served by GET /api/product/{id}/image
ALTER TABLE product DROP COLUMN IF EXISTS image_url;
