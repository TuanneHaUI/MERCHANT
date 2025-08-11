1: add index
- CREATE INDEX idx_mechants_status ON merchant.merchants(status);

- CREATE INDEX idx_merchant_status_account
  ON merchant.merchants (merchant_id, status, account_no);

-CREATE INDEX idx_email ON users(email);

-CREATE INDEX idx_refresh_token ON users(refresh_token(255));
