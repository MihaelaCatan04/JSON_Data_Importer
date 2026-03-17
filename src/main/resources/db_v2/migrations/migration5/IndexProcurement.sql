CREATE UNIQUE INDEX procurement_idx ON procurement (
                                                    COALESCE(date_of_order, TIMESTAMPTZ '0001-01-01 00:00:00+00'),
                                                    title,
                                                    amount,
                                                    COALESCE(government_departments, ''),
                                                    COALESCE(note, '')
    )
    WHERE deleted IS false;
