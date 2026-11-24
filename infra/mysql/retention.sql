-- Keeps the database at a roughly constant size while the load generator runs (~5 orders/s).
-- Every minute, delete rows created by live traffic more than 1 hour ago.
-- Runs after init.sql on first start; to apply to an existing volume:
--   docker compose exec -T mysql mysql -uroot -proot < infra/mysql/retention.sql
-- Needs event_scheduler=ON (the MySQL 8 default).

-- The first 2,000,000 stock_movement rows are the seeded history that fault F01 depends on.
-- Never delete them: only rows with id > 2000000 come from live traffic.
DROP EVENT IF EXISTS inventory.prune_stock_movement;
CREATE EVENT inventory.prune_stock_movement
    ON SCHEDULE EVERY 1 MINUTE
    DO DELETE FROM inventory.stock_movement
       WHERE id > 2000000 AND created_at < NOW() - INTERVAL 1 HOUR
       LIMIT 50000;

DROP EVENT IF EXISTS orders.prune_orders;
CREATE EVENT orders.prune_orders
    ON SCHEDULE EVERY 1 MINUTE
    DO DELETE FROM orders.orders
       WHERE created_at < NOW() - INTERVAL 1 HOUR
       LIMIT 50000;

DROP EVENT IF EXISTS orders.prune_order_audit;
CREATE EVENT orders.prune_order_audit
    ON SCHEDULE EVERY 1 MINUTE
    DO DELETE FROM orders.order_audit
       WHERE created_at < NOW() - INTERVAL 1 HOUR
       LIMIT 50000;
