CREATE TABLE certification
(
    certification_id       BIGINT DEFAULT nextval('certification_id_seq') PRIMARY KEY,
    date_of_approval       DATE NOT NULL,
    title                  VARCHAR(255) NOT NULL,
    amount                 BIGINT NOT NULL,
    target                 VARCHAR(255),
    government_departments VARCHAR(255),
    category               VARCHAR(255),
    deleted                BOOLEAN
)