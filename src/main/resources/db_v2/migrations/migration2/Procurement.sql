CREATE TABLE procurement
(
    procurement_id         BIGINT DEFAULT nextval('procurement_id_seq') PRIMARY KEY,
    date_of_order          TIMESTAMPTZ,
    title                  VARCHAR(255) NOT NULL,
    amount                 BIGINT NOT NULL,
    government_departments VARCHAR(255),
    note                   TEXT,
    deleted                BOOLEAN
);