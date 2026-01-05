CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name  VARCHAR(100) NOT NULL,
  email      VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled    TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
);


CREATE TABLE portfolios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  base_currency CHAR(3) NOT NULL DEFAULT 'EUR',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_portfolios_user (user_id),
  CONSTRAINT fk_portfolios_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
);

CREATE TABLE assets (
  id BIGINT NOT NULL AUTO_INCREMENT,
  asset_type VARCHAR(20) NOT NULL,       -- ETF, STOCK, BOND, CRYPTO, CASH
  symbol VARCHAR(64) NULL,               -- es. AAPL, BTC, ecc.
  isin VARCHAR(32) NULL,                 -- utile per ETF/bond
  name VARCHAR(255) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_assets_symbol_type (asset_type, symbol),
  UNIQUE KEY uk_assets_isin (isin)
);

CREATE TABLE portfolio_holdings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  portfolio_id BIGINT NOT NULL,
  asset_id BIGINT NOT NULL,

  quantity DECIMAL(28,10) NOT NULL DEFAULT 0,  -- quote, azioni, coin; per CASH puoi usare 1 riga con quantity=saldo
  avg_price DECIMAL(28,10) NULL,               -- opzionale: prezzo medio di carico
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_portfolio_asset (portfolio_id, asset_id),
  KEY ix_holdings_portfolio (portfolio_id),
  KEY ix_holdings_asset (asset_id),

  CONSTRAINT fk_holdings_portfolio
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_holdings_asset
    FOREIGN KEY (asset_id) REFERENCES assets(id)
    ON DELETE RESTRICT
);

