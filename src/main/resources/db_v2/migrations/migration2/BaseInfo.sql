CREATE TABLE base_info
(
    base_info_id                               BIGINT DEFAULT nextval('base_info_id_seq') PRIMARY KEY,
    average_continuous_service_years_type      VARCHAR(255),
    average_continuous_service_years_male      DOUBLE PRECISION,
    average_continuous_service_years_female    DOUBLE PRECISION,
    average_continuous_service_years           DOUBLE PRECISION,
    average_age                                DOUBLE PRECISION,
    month_average_predetermined_overtime_hours DOUBLE PRECISION,
    deleted                                    BOOLEAN
)