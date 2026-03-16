CREATE UNIQUE INDEX commendation_idx
    ON commendation (
                     COALESCE(date_of_commendation, DATE '0001-01-01'),
                     title,
                     COALESCE(target, ''),
                     COALESCE(category, ''),
                     COALESCE(government_departments, ''),
                     COALESCE(note, '')
        )
    WHERE deleted IS false