CREATE TABLE import_checkpoint
(
    zip_entry_name VARCHAR(255) PRIMARY KEY,
    completed_at   TIMESTAMPTZ DEFAULT now()
);