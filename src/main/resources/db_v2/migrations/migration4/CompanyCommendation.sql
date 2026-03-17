CREATE TABLE company_commendation
(
    company_id      BIGINT,
    commendation_id BIGINT,
    update_date     TIMESTAMPTZ,
    inserted_at     TIMESTAMPTZ,
    deleted         BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (commendation_id) REFERENCES commendation (commendation_id),
    UNIQUE (company_id, commendation_id)
)