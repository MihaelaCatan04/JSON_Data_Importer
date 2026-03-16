CREATE TABLE company_item
(
    company_id BIGINT NOT NULL REFERENCES company (company_id),
    info_id    BIGINT NOT NULL REFERENCES item_info (info_id),
    UNIQUE (company_id, info_id)
);