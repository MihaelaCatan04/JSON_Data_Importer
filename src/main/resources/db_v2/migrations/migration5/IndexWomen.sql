CREATE UNIQUE INDEX women_idx
    ON women_activity_info (
                            COALESCE(female_workers_proportion_type, ''),
                            COALESCE(female_workers_proportion, 0),
                            COALESCE(female_share_of_manager, 0),
                            COALESCE(gender_total_of_manager, 0),
                            COALESCE(female_share_of_officers, 0),
                            COALESCE(gender_total_of_officers, 0)
        )
    WHERE deleted is FALSE;