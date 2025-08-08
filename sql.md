1: add index
- CREATE INDEX idx_mechants_status ON merchant.merchants(status);

- CREATE INDEX idx_merchant_status_account
  ON merchant.merchants (merchant_id, status, account_no);
