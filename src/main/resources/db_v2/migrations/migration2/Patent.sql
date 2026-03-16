CREATE TABLE patent
(
    patent_id           BIGINT DEFAULT nextval('patent_id_seq') PRIMARY KEY,
    patent_type         VARCHAR(255) NOT NULL,
    registration_number VARCHAR(255) NOT NULL,
    application_date    DATE,
    title               TEXT NOT NULL,
    url                 TEXT,
    deleted             BOOLEAN
)