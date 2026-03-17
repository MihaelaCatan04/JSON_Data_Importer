CREATE UNIQUE INDEX subsidy_idx
    ON subsidy (
                COALESCE(date_of_approval, DATE '0001-01-01'),
                title,
                amount,
                COALESCE(target, ''),
                COALESCE(government_departments, '')
        )
    WHERE deleted IS false;