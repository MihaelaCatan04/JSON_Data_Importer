CREATE TABLE stg_company
(
    corporate_number      VARCHAR(13),
    name                  VARCHAR(255),
    kana                  VARCHAR(255),
    name_en               VARCHAR(255),
    postal_code           VARCHAR(255),
    location              VARCHAR(255),
    process               VARCHAR(255),
    aggregated_year       VARCHAR(1),
    status                VARCHAR(255),
    close_date            DATE,
    close_cause           VARCHAR(255),
    kind                  VARCHAR(10),
    representative_name   VARCHAR(255),
    capital_stock         BIGINT,
    employee_number       INTEGER,
    company_size_male     INTEGER,
    company_size_female   INTEGER,
    business_summary      TEXT,
    company_url           TEXT,
    founding_year         INTEGER,
    date_of_establishment DATE,
    qualification_grade   VARCHAR(255),
    update_date           TIMESTAMPTZ
);

CREATE TABLE stg_company_item
(
    corporate_number VARCHAR(13),
    value            VARCHAR(255),
    is_industry      BOOLEAN
);

CREATE TABLE stg_patent
(
    corporate_number    VARCHAR(13),
    merge_key           TEXT,
    patent_type         VARCHAR(255),
    registration_number VARCHAR(255),
    application_date    DATE,
    title               TEXT,
    url                 TEXT
);

CREATE TABLE stg_classification
(
    patent_merge_key TEXT,
    merge_key        TEXT,
    code_value       VARCHAR(255),
    code_name        VARCHAR(255),
    japanese         VARCHAR(255)
);

CREATE TABLE stg_certification
(
    corporate_number       VARCHAR(13),
    merge_key              TEXT,
    date_of_approval       DATE,
    title                  VARCHAR(255),
    target                 VARCHAR(255),
    government_departments VARCHAR(255),
    category               VARCHAR(255)
);

CREATE TABLE stg_subsidy
(
    corporate_number       VARCHAR(13),
    merge_key              TEXT,
    date_of_approval       DATE,
    title                  VARCHAR(255),
    amount                 BIGINT,
    target                 VARCHAR(255),
    government_departments VARCHAR(255)
);

CREATE TABLE stg_commendation
(
    corporate_number       VARCHAR(13),
    merge_key              TEXT,
    date_of_commendation   DATE,
    title                  VARCHAR(255),
    target                 TEXT,
    category               VARCHAR(255),
    government_departments VARCHAR(255),
    note                   TEXT
);

CREATE TABLE stg_procurement
(
    corporate_number       VARCHAR(13),
    merge_key              TEXT,
    date_of_order          TIMESTAMPTZ,
    title                  VARCHAR(255),
    amount                 BIGINT,
    government_departments VARCHAR(255),
    note                   TEXT
);

CREATE TABLE stg_base_info
(
    corporate_number                           VARCHAR(13),
    merge_key                                  TEXT,
    average_continuous_service_years_type      VARCHAR(255),
    average_continuous_service_years_male      DOUBLE PRECISION,
    average_continuous_service_years_female    DOUBLE PRECISION,
    average_continuous_service_years           DOUBLE PRECISION,
    average_age                                DOUBLE PRECISION,
    month_average_predetermined_overtime_hours DOUBLE PRECISION
);

CREATE TABLE stg_women_activity
(
    corporate_number               VARCHAR(13),
    merge_key                      TEXT,
    female_workers_proportion_type VARCHAR(255),
    female_workers_proportion      DOUBLE PRECISION,
    female_share_of_manager        DOUBLE PRECISION,
    gender_total_of_manager        DOUBLE PRECISION,
    female_share_of_officers       DOUBLE PRECISION,
    gender_total_of_officers       DOUBLE PRECISION
);

CREATE TABLE stg_compat_childcare
(
    corporate_number                VARCHAR(13),
    merge_key                       TEXT,
    number_of_paternity_leave       INTEGER,
    number_of_maternity_leave       INTEGER,
    paternity_leave_acquisition_num INTEGER,
    maternity_leave_acquisition_num INTEGER
);

CREATE TABLE stg_finance
(
    corporate_number       VARCHAR(13),
    merge_key              TEXT,
    accounting_standards   VARCHAR(255),
    fiscal_year_cover_page TEXT
);

CREATE TABLE stg_major_shareholder
(
    finance_merge_key       TEXT,
    merge_key               TEXT,
    name_major_stakeholders VARCHAR(255),
    shareholding_ratio      DOUBLE PRECISION
);

CREATE TABLE stg_management_index
(
    finance_merge_key                                             TEXT,
    merge_key                                                     TEXT,
    period                                                        INTEGER,
    net_sales_summary_of_business_results                         BIGINT,
    net_sales_summary_of_business_results_unit_ref                VARCHAR(10),
    operating_revenue1_summary_of_business_results                BIGINT,
    operating_revenue1_summary_of_business_results_unit_ref       VARCHAR(10),
    operating_revenue2_summary_of_business_results                BIGINT,
    operating_revenue2_summary_of_business_results_unit_ref       VARCHAR(10),
    gross_operating_revenue_summary_of_business_results           BIGINT,
    gross_operating_revenue_summary_of_business_results_unit_ref  VARCHAR(10),
    ordinary_income_summary_of_business_results                   BIGINT,
    ordinary_income_summary_of_business_results_unit_ref          VARCHAR(10),
    net_premiums_written_summary_of_business_results_ins          BIGINT,
    net_premiums_written_summary_of_business_results_ins_unit_ref VARCHAR(10),
    ordinary_income_loss_summary_of_business_results              BIGINT,
    ordinary_income_loss_summary_of_business_results_unit_ref     VARCHAR(10),
    net_income_loss_summary_of_business_results                   BIGINT,
    net_income_loss_summary_of_business_results_unit_ref          VARCHAR(10),
    capital_stock_summary_of_business_results                     BIGINT,
    capital_stock_summary_of_business_results_unit_ref            VARCHAR(10),
    net_assets_summary_of_business_results                        BIGINT,
    net_assets_summary_of_business_results_unit_ref               VARCHAR(10),
    total_assets_summary_of_business_results                      BIGINT,
    total_assets_summary_of_business_results_unit_ref             VARCHAR(10),
    number_of_employees                                           BIGINT,
    number_of_employees_unit_ref                                  VARCHAR(10)
);

CREATE INDEX idx_stg_company_corporate_number ON stg_company (corporate_number);
CREATE INDEX idx_stg_company_item_corporate_number ON stg_company_item (corporate_number);
CREATE INDEX idx_stg_company_item_value_industry ON stg_company_item (value, is_industry);

CREATE INDEX idx_stg_patent_company_merge_key ON stg_patent (corporate_number, merge_key);
CREATE INDEX idx_stg_classification_patent_merge_key ON stg_classification (patent_merge_key, merge_key);

CREATE INDEX idx_stg_certification_company_merge_key ON stg_certification (corporate_number, merge_key);
CREATE INDEX idx_stg_subsidy_company_merge_key ON stg_subsidy (corporate_number, merge_key);
CREATE INDEX idx_stg_commendation_company_merge_key ON stg_commendation (corporate_number, merge_key);
CREATE INDEX idx_stg_procurement_company_merge_key ON stg_procurement (corporate_number, merge_key);

CREATE INDEX idx_stg_base_info_company_merge_key ON stg_base_info (corporate_number, merge_key);
CREATE INDEX idx_stg_women_activity_company_merge_key ON stg_women_activity (corporate_number, merge_key);
CREATE INDEX idx_stg_compat_childcare_company_merge_key ON stg_compat_childcare (corporate_number, merge_key);

CREATE INDEX idx_stg_finance_company_merge_key ON stg_finance (corporate_number, merge_key);
CREATE INDEX idx_stg_major_shareholder_finance_merge_key ON stg_major_shareholder (finance_merge_key, merge_key);
CREATE INDEX idx_stg_management_index_finance_merge_key ON stg_management_index (finance_merge_key, merge_key);

CREATE TABLE IF NOT EXISTS run_company
(
    corporate_number text PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS run_company_item
(
    corporate_number text    NOT NULL,
    value            text    NOT NULL,
    is_industry      boolean NOT NULL,
    PRIMARY KEY (corporate_number, value, is_industry)
);

CREATE TABLE IF NOT EXISTS run_patent
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_classification
(
    patent_merge_key text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (patent_merge_key, merge_key)
);

CREATE TABLE IF NOT EXISTS run_certification
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_subsidy
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_commendation
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_procurement
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_finance
(
    corporate_number text NOT NULL,
    merge_key        text NOT NULL,
    PRIMARY KEY (corporate_number, merge_key)
);

CREATE TABLE IF NOT EXISTS run_major_shareholder
(
    finance_merge_key text NOT NULL,
    merge_key         text NOT NULL,
    PRIMARY KEY (finance_merge_key, merge_key)
);

CREATE TABLE IF NOT EXISTS run_management_index
(
    finance_merge_key text NOT NULL,
    merge_key         text NOT NULL,
    PRIMARY KEY (finance_merge_key, merge_key)
);

CREATE TABLE IF NOT EXISTS stg_company_workplace_parts
(
    corporate_number                       varchar(13) PRIMARY KEY,
    base_info_id                           bigint,
    women_activity_info_id                 bigint,
    compatibility_of_childcare_and_work_id bigint,
    merge_key                              varchar(32)
);