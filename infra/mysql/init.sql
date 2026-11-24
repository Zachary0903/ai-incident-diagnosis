-- Runs once, on first start of the mysql container (empty volume).
-- Re-run with: docker compose down -v && docker compose up -d

CREATE DATABASE IF NOT EXISTS orders;
CREATE DATABASE IF NOT EXISTS inventory;
GRANT ALL PRIVILEGES ON orders.* TO 'app'@'%';
GRANT ALL PRIVILEGES ON inventory.* TO 'app'@'%';
FLUSH PRIVILEGES;

-- ---------------------------------------------------------------- orders
USE orders;

CREATE TABLE orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku              VARCHAR(32) NOT NULL,
    qty              INT         NOT NULL,
    promo_code       VARCHAR(32),
    unit_price_cents BIGINT      NOT NULL,   -- snapshot of inventory.product.price_cents at order time
    total_cents      BIGINT      NOT NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_audit (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT      NOT NULL,
    event      VARCHAR(32) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------- inventory
USE inventory;

CREATE TABLE product (
    sku         VARCHAR(32) PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    price_cents BIGINT       NOT NULL,   -- current list price; served by GET /products/{sku}
    stock       BIGINT       NOT NULL
);

CREATE TABLE stock_movement (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku        VARCHAR(32) NOT NULL,
    qty        INT         NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SET SESSION cte_max_recursion_depth = 2000001;

-- 1000 products: SKU-0000 .. SKU-0999, priced $0.99 .. $499.99 (spread pseudo-randomly)
INSERT INTO product (sku, name, price_cents, stock)
WITH RECURSIVE seq (n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM seq WHERE n < 999)
SELECT CONCAT('SKU-', LPAD(n, 4, '0')), CONCAT('Product ', n), (1 + (n * 7919) % 500) * 100 - 1, 1000000
FROM seq;

-- ~2M historical movements (2000 per SKU). Without idx_sku (fault F01), every
-- velocity check in /inventory/reserve becomes a full scan of this table.
INSERT INTO stock_movement (sku, qty, created_at)
WITH RECURSIVE seq (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 2000000)
SELECT CONCAT('SKU-', LPAD(n % 1000, 4, '0')),
       IF(n % 3 = 0, 50, -1),
       NOW() - INTERVAL (n % 525600) MINUTE
FROM seq;

CREATE INDEX idx_sku ON stock_movement (sku);
