CREATE TABLE item_info
(
    info_id     BIGINT PRIMARY KEY DEFAULT nextval('item_info_id_seq'),
    value       VARCHAR(5) NOT NULL,
    is_industry BOOLEAN    NOT NULL,
    deleted     BOOLEAN,
    inserted_at TIMESTAMPTZ
);