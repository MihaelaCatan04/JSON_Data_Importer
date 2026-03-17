CREATE TABLE company_procurement
(
    company_id     BIGINT,
    procurement_id BIGINT,
    update_date    TIMESTAMPTZ,
    inserted_at    TIMESTAMPTZ,
    deleted        BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (procurement_id) REFERENCES procurement (procurement_id),
    UNIQUE (company_id, procurement_id)
)