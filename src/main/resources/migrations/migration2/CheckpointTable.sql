CREATE TABLE import_checkpoint
(
    run_id        VARCHAR(36) NOT NULL DEFAULT '',
    zip_entry_name VARCHAR(255) NOT NULL,
    completed_at   TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (run_id, zip_entry_name)
);