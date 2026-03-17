CREATE TABLE company_certification
(
    company_id       BIGINT,
    certification_id BIGINT,
    update_date      TIMESTAMPTZ,
    inserted_at      TIMESTAMPTZ,
    deleted          BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (certification_id) REFERENCES certification (certification_id),
    UNIQUE (company_id, certification_id)
)