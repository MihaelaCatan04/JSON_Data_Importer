CREATE TABLE classification
(
    classification_id BIGINT DEFAULT nextval('classification_id_seq') PRIMARY KEY,
    code_value        VARCHAR(255),
    code_name         VARCHAR(255),
    japanese          VARCHAR(255),
    deleted           BOOLEAN
);
