CREATE UNIQUE INDEX compatibility_idx
    ON compatibility_of_childcare_and_work (
                                            COALESCE(number_of_paternity_leave, 0),
                                            COALESCE(number_of_maternity_leave, 0),
                                            COALESCE(paternity_leave_acquisition_num, 0),
                                            COALESCE(maternity_leave_acquisition_num, 0)
        )
    WHERE deleted IS false;