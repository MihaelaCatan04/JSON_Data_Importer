CREATE UNIQUE INDEX subsidy_idx
    ON subsidy (
                COALESCE(date_of_approval::text, ''),
                title,
                amount,
                COALESCE(target, ''),
                COALESCE(government_departments, '')
        )
    WHERE deleted IS false;