CREATE TABLE finance
(
    finance_id             BIGINT DEFAULT nextval('finance_id_seq') PRIMARY KEY,
    accounting_standards   VARCHAR(255),
    fiscal_year_cover_page TEXT,
    deleted                BOOLEAN
);
