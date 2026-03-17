CREATE TABLE compatibility_of_childcare_and_work
(
    compatibility_of_childcare_and_work_id BIGINT DEFAULT nextval('compatibility_id_seq') PRIMARY KEY,
    number_of_paternity_leave              INTEGER,
    number_of_maternity_leave              INTEGER,
    paternity_leave_acquisition_num        INTEGER,
    maternity_leave_acquisition_num        INTEGER,
    deleted                                BOOLEAN
)