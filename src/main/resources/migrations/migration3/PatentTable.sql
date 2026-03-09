CREATE TABLE patent
(
    patent_id           BIGINT DEFAULT nextval('patent_id_seq') PRIMARY KEY,
    company_id          BIGINT,
    patent_type         VARCHAR(255),
    registration_number VARCHAR(255),
    application_date    DATE,
    title               TEXT,
    url                 TEXT,
    update_date         TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ,
    inserted_at         TIMESTAMPTZ,
    FOREIGN KEY (company_id) REFERENCES company (company_id) ON DELETE CASCADE
);