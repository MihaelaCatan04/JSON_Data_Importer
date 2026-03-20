CREATE TABLE import_execution_state
(
    window_key  VARCHAR(100) NOT NULL,
    window_date DATE         NOT NULL,
    running     BOOLEAN      NOT NULL,
    success     BOOLEAN      NOT NULL,
    inserted_at TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP,
    PRIMARY KEY (window_key, window_date)
);