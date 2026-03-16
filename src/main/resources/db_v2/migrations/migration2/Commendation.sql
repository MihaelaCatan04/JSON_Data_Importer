CREATE TABLE commendation
(
    commendation_id        BIGINT DEFAULT nextval('commendation_id_seq') PRIMARY KEY,
    date_of_commendation   DATE,
    title                  VARCHAR(255) NOT NULL,
    target                 TEXT,
    category               VARCHAR(255),
    government_departments VARCHAR(255),
    note                   TEXT,
    deleted                BOOLEAN
);