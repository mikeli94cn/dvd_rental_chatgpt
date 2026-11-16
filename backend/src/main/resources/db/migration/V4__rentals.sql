CREATE TABLE rental_orders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_orders_customer (customer_id, created_at)
);
CREATE TABLE rental_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    copy_id BIGINT NOT NULL,
    due_date DATE NOT NULL,
    returned_at TIMESTAMP(6) NULL,
    status VARCHAR(20) NOT NULL,
    daily_rate_snapshot DECIMAL(10,2) NOT NULL,
    overdue_rate_snapshot DECIMAL(10,2) NOT NULL,
    rental_charge DECIMAL(12,2) NOT NULL,
    overdue_charge DECIMAL(12,2) NOT NULL DEFAULT 0,
    confirmed_by BIGINT NULL,
    active_copy_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN copy_id ELSE NULL END) STORED,
    UNIQUE KEY uq_active_rental_copy (active_copy_id),
    FOREIGN KEY (order_id) REFERENCES rental_orders(id),
    FOREIGN KEY (copy_id) REFERENCES dvd_copies(id),
    FOREIGN KEY (confirmed_by) REFERENCES users(id),
    CONSTRAINT ck_item_status CHECK (status IN ('ACTIVE', 'RETURNED', 'LOST')),
    CONSTRAINT ck_item_resolution CHECK ((status = 'ACTIVE' AND returned_at IS NULL AND confirmed_by IS NULL)
        OR (status <> 'ACTIVE' AND returned_at IS NOT NULL AND confirmed_by IS NOT NULL)),
    CONSTRAINT ck_rental_money CHECK (daily_rate_snapshot >= 0 AND overdue_rate_snapshot >= 0 AND rental_charge >= 0 AND overdue_charge >= 0),
    INDEX idx_items_overdue (status, due_date)
);
