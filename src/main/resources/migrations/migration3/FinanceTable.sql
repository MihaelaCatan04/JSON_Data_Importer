CREATE TABLE finance
(
    finance_id             BIGINT DEFAULT nextval('finance_id_seq') PRIMARY KEY,
    company_id             BIGINT,
    accounting_standards   VARCHAR(255),
    fiscal_year_cover_page TEXT,
    update_date            TIMESTAMPTZ,
    updated_at             TIMESTAMPTZ,
    inserted_at            TIMESTAMPTZ,
    FOREIGN KEY (company_id) REFERENCES company (company_id) ON DELETE CASCADE
);
