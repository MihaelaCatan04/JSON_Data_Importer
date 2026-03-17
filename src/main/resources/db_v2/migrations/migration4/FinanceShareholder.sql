CREATE TABLE finance_shareholder
(
    finance_id           BIGINT,
    major_shareholder_id BIGINT,
    update_date          TIMESTAMPTZ,
    inserted_at          TIMESTAMPTZ,
    deleted              BOOLEAN,
    FOREIGN KEY (finance_id) REFERENCES finance (finance_id),
    FOREIGN KEY (major_shareholder_id) REFERENCES major_shareholder (major_shareholder_id),
    UNIQUE (finance_id, major_shareholder_id)
)