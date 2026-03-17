CREATE TABLE women_activity_info
(
    women_activity_info_id         BIGINT DEFAULT nextval('women_activity_id_seq') PRIMARY KEY,
    female_workers_proportion_type VARCHAR(255),
    female_workers_proportion      DOUBLE PRECISION,
    female_share_of_manager        DOUBLE PRECISION,
    gender_total_of_manager        DOUBLE PRECISION,
    female_share_of_officers       DOUBLE PRECISION,
    gender_total_of_officers       DOUBLE PRECISION,
    deleted                        BOOLEAN
);