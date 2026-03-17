CREATE UNIQUE INDEX base_info_idx
    ON base_info (
                  COALESCE(average_continuous_service_years_type, ''),
                  COALESCE(average_continuous_service_years_male, 0),
                  COALESCE(average_continuous_service_years_female, 0),
                  COALESCE(average_continuous_service_years, 0),
                  COALESCE(average_age, 0),
                  COALESCE(month_average_predetermined_overtime_hours, 0)
        )
WHERE deleted IS false;