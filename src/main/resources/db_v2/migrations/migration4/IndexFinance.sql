CREATE UNIQUE INDEX finance_idx
    ON finance (
                COALESCE(accounting_standards, ''),
                COALESCE(fiscal_year_cover_page, '')
        )
    WHERE deleted IS false