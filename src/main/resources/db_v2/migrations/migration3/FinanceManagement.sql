CREATE TABLE finance_management
(
    finance_id          BIGINT,
    management_index_id BIGINT,
    update_date         TIMESTAMPTZ,
    inserted_at         TIMESTAMPTZ,
    deleted             BOOLEAN,
    FOREIGN KEY (finance_id) REFERENCES finance (finance_id),
    FOREIGN KEY (management_index_id) REFERENCES management_index (management_index_id),
    UNIQUE (finance_id, management_index_id)
)