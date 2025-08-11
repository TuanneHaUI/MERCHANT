1: add index
- CREATE INDEX idx_mechants_status ON merchant.merchants(status);

- CREATE INDEX idx_merchant_status_account
  ON merchant.merchants (merchant_id, status, account_no);

- CREATE INDEX idx_transaction_merchant_date
  ON merchant.transactions (merchant_id, transaction_date);

- ALTER TABLE merchant.transactions
  ADD INDEX idx_transaction_date_merchant (transaction_date, merchant_id, status);

- CREATE INDEX idx_merchants_open_date_status ON merchant.merchants(status, open_date);

- CREATE INDEX idx_merchants_close_date_status ON merchant.merchants(status, close_date);

2: add partition
-- Tìm tên foreign key
- SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = 'merchant'
  AND table_name = 'transactions'
  AND referenced_table_name IS NOT NULL;

-- Xoá foreign key
- ALTER TABLE merchant.transactions DROP FOREIGN KEY <tên_fk>;

- ALTER TABLE merchant.transactions
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (id, transaction_date, merchant_id);

- ALTER TABLE merchant.transactions
  PARTITION BY RANGE (YEAR(transaction_date)) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION pmax VALUES LESS THAN MAXVALUE
  );
