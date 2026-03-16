CREATE TABLE subsidy
(
    subsidy_id         BIGINT DEFAULT nextval('subsidy_id_seq') PRIMARY KEY,
    date_of_approval       DATE,
    title                  VARCHAR(255) NOT NULL,
    amount                 BIGINT NOT NULL,
    target                 VARCHAR(255),
    government_departments VARCHAR(255),
    deleted                BOOLEAN
)