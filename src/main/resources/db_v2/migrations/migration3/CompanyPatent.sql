CREATE TABLE company_patent
(
    company_id  BIGINT,
    patent_id   BIGINT,
    update_date TIMESTAMPTZ,
    inserted_at TIMESTAMPTZ,
    deleted     BOOLEAN,
    FOREIGN KEY (company_id) REFERENCES company (company_id),
    FOREIGN KEY (patent_id) REFERENCES patent (patent_id),
    UNIQUE (company_id, patent_id)
)