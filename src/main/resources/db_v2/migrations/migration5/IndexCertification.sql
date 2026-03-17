CREATE UNIQUE INDEX certification_idx
    ON certification (
                      date_of_approval,
                      title,
                      amount,
                      COALESCE(target, ''),
                      COALESCE(government_departments, ''),
                      COALESCE(category, '')
        )
    WHERE deleted IS false