CREATE TABLE currency (
    currency_id SERIAL PRIMARY KEY,                      -- Auto-incremented unique identifier for each currency
    currency_code VARCHAR(3) UNIQUE NOT NULL,               -- ISO 4217 currency code (e.g., USD, EUR)
    currency_name VARCHAR(255) NOT NULL,             -- Full name of the currency (e.g., United States Dollar)
    currency_symbol VARCHAR(3)                       -- Optional: Symbol of the currency (e.g., $)
);

CREATE TABLE deal (
    deal_id SERIAL PRIMARY KEY,                      -- Auto-incremented unique identifier for each deal
    deal_unique_id VARCHAR(255) UNIQUE NOT NULL,     -- Provided unique ID for each deal (must be unique)
    from_currency_id INTEGER NOT NULL,             -- Currency code for the currency being sold (foreign key)
    to_currency_id INTEGER NOT NULL,               -- Currency code for the currency being bought (foreign key)
    deal_timestamp TIMESTAMP NOT NULL,               -- Timestamp of when the deal was made
    deal_amount NUMERIC(18, 2) NOT NULL,             -- Deal amount in the "from" currency (with 2 decimal places)
    
    CONSTRAINT fk_from_currency
        FOREIGN KEY (from_currency_id) 
        REFERENCES currency (currency_id)
        ON DELETE RESTRICT,                         
    
    CONSTRAINT fk_to_currency
        FOREIGN KEY (to_currency_id)
        REFERENCES currency (currency_id)
        ON DELETE RESTRICT                           
);

-- Indexes for optimization
CREATE INDEX idx_deals_unique_id ON deal (deal_unique_id);  -- Index on deal_unique_id to ensure fast lookups and enforce uniqueness


-- insert values into currencies table
INSERT INTO currency (currency_code, currency_name, currency_symbol)
VALUES 
    ('USD', 'United States Dollar', '$'),
    ('EUR', 'Euro', '€'),
    ('GBP', 'British Pound Sterling', '£'),
    ('JPY', 'Japanese Yen', '¥'),
    ('AUD', 'Australian Dollar', 'A$'),
    ('CAD', 'Canadian Dollar', 'C$'),
    ('CHF', 'Swiss Franc', 'CHF'),
    ('CNY', 'Chinese Yuan', '¥'),
    ('SEK', 'Swedish Krona', 'kr'),
    ('NZD', 'New Zealand Dollar', 'NZ$'),
    ('AED', 'United Arab Emirates Dirham', 'د.إ'),
    ('SAR', 'Saudi Riyal', '﷼'),
    ('EGP', 'Egyptian Pound', '£'),
    ('QAR', 'Qatari Riyal', '﷼'),
    ('KWD', 'Kuwaiti Dinar', 'د.ك'),
    ('OMR', 'Omani Rial', '﷼'),
    ('BHD', 'Bahraini Dinar', 'ب.د'),
    ('LYD', 'Libyan Dinar', 'ل.د'),
    ('IQD', 'Iraqi Dinar', 'ع.د'),
    ('JOD', 'Jordanian Dinar', 'د.ا');
