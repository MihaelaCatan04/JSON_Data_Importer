CREATE TABLE company_finance
(
    company_id  BIGINT,
    finance_id  BIGINT,
    update_date TIMESTAMPTZ,
    inserted_at TIMESTAMPTZ,
    deleted     BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (finance_id) REFERENCES finance (finance_id),
    UNIQUE (company_id, finance_id)
)