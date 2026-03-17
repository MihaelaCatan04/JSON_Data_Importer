CREATE TABLE workplace_info
(
    workplace_info_id                   BIGINT DEFAULT nextval('workplace_info_id_seq') PRIMARY KEY,
    base_info                           BIGINT,
    women_activity_info                 BIGINT,
    compatibility_of_childcare_and_work BIGINT,
    update_date                         TIMESTAMPTZ,
    inserted_at                         TIMESTAMPTZ,
    deleted                             BOOLEAN,
    FOREIGN KEY (base_info) REFERENCES base_info (base_info_id),
    FOREIGN KEY (women_activity_info) REFERENCES women_activity_info (women_activity_info_id),
    FOREIGN KEY (compatibility_of_childcare_and_work) REFERENCES compatibility_of_childcare_and_work (compatibility_of_childcare_and_work_id)
);