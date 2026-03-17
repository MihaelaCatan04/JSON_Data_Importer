CREATE UNIQUE INDEX patent_idx
    ON patent (
               patent_type,
               registration_number,
               COALESCE(application_date, DATE '0001-01-01'),
               title,
               COALESCE(url, '')
        )
    WHERE deleted IS false;