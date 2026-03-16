CREATE TABLE patent_classification
(
    patent_id         BIGINT,
    classification_id BIGINT,
    update_date       TIMESTAMPTZ,
    inserted_at       TIMESTAMPTZ,
    deleted           BOOLEAN,
    FOREIGN KEY (patent_id) REFERENCES patent (patent_id),
    FOREIGN KEY (classification_id) REFERENCES classification (classification_id),
    UNIQUE (patent_id, classification_id)
)