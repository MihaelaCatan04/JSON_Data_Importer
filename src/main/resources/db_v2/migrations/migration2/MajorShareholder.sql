CREATE TABLE major_shareholder
(
    major_shareholder_id    BIGINT DEFAULT nextval('major_shareholder_id_seq') PRIMARY KEY,
    name_major_stakeholders VARCHAR(255) NOT NULL,
    shareholding_ratio      DOUBLE PRECISION NOT NULL,
    deleted                 BOOLEAN
);