CREATE TABLE workplace_info
(
    workplace_info_id BIGINT DEFAULT nextval('workplace_info_id_seq') PRIMARY KEY,
    company_id        BIGINT,
    update_date       TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ,
    inserted_at       TIMESTAMPTZ,
    FOREIGN KEY (company_id) REFERENCES company (company_id) ON DELETE CASCADE
);