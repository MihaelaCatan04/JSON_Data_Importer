CREATE UNIQUE INDEX workplace_idx
    ON workplace_info (
                       base_info,
                       women_activity_info,
                       compatibility_of_childcare_and_work
        )
WHERE deleted IS false;