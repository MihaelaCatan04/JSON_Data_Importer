CREATE TABLE company_subsidy
(
    company_id  BIGINT,
    subsidy_id  BIGINT,
    update_date TIMESTAMPTZ,
    inserted_at TIMESTAMPTZ,
    deleted     BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (subsidy_id) REFERENCES subsidy (subsidy_id),
    UNIQUE (company_id, subsidy_id)
)