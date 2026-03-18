package com.java.gbizinfo.importer.util;

public record CopyUtil() {
    public static final String COPY_METHOD = "com.java.gbizinfo.importer.mapper.DataMapper.copy";

    public static final String COPY_STG_COMPANY = "COPY stg_company (corporate_number,name,kana,name_en,postal_code,location,process,aggregated_year,status,close_date,close_cause,kind,representative_name,capital_stock,employee_number,company_size_male,company_size_female,business_summary,company_url,founding_year,date_of_establishment,qualification_grade,update_date) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_COMPANY_ITEM = "COPY stg_company_item (corporate_number,value,is_industry) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_PATENT = "COPY stg_patent (corporate_number,merge_key,patent_type,registration_number,application_date,title,url) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_CLASSIFICATION = "COPY stg_classification (patent_merge_key,merge_key,code_value,code_name,japanese) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_CERTIFICATION = "COPY stg_certification (corporate_number,merge_key,date_of_approval,title,target,government_departments,category) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_SUBSIDY = "COPY stg_subsidy (corporate_number,merge_key,date_of_approval,title,amount,target,government_departments) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_COMMENDATION = "COPY stg_commendation (corporate_number,merge_key,date_of_commendation,title,target,category,government_departments,note) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_PROCUREMENT = "COPY stg_procurement (corporate_number,merge_key,date_of_order,title,amount,government_departments,note) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_BASE_INFO = "COPY stg_base_info (corporate_number,merge_key,average_continuous_service_years_type,average_continuous_service_years_male,average_continuous_service_years_female,average_continuous_service_years,average_age,month_average_predetermined_overtime_hours) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_WOMEN_ACTIVITY = "COPY stg_women_activity (corporate_number,merge_key,female_workers_proportion_type,female_workers_proportion,female_share_of_manager,gender_total_of_manager,female_share_of_officers,gender_total_of_officers) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_COMPAT_CHILDCARE = "COPY stg_compat_childcare (corporate_number,merge_key,number_of_paternity_leave,number_of_maternity_leave,paternity_leave_acquisition_num,maternity_leave_acquisition_num) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_FINANCE = "COPY stg_finance (corporate_number,merge_key,accounting_standards,fiscal_year_cover_page) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_MAJOR_SHAREHOLDER = "COPY stg_major_shareholder (finance_merge_key,merge_key,name_major_stakeholders,shareholding_ratio) FROM STDIN WITH (FORMAT csv, NULL '')";

    public static final String COPY_STG_MANAGEMENT_INDEX = "COPY stg_management_index (finance_merge_key,merge_key,period,net_sales_summary_of_business_results,net_sales_summary_of_business_results_unit_ref,operating_revenue1_summary_of_business_results,operating_revenue1_summary_of_business_results_unit_ref,operating_revenue2_summary_of_business_results,operating_revenue2_summary_of_business_results_unit_ref,gross_operating_revenue_summary_of_business_results,gross_operating_revenue_summary_of_business_results_unit_ref,ordinary_income_summary_of_business_results,ordinary_income_summary_of_business_results_unit_ref,net_premiums_written_summary_of_business_results_ins,net_premiums_written_summary_of_business_results_ins_unit_ref,ordinary_income_loss_summary_of_business_results,ordinary_income_loss_summary_of_business_results_unit_ref,net_income_loss_summary_of_business_results,net_income_loss_summary_of_business_results_unit_ref,capital_stock_summary_of_business_results,capital_stock_summary_of_business_results_unit_ref,net_assets_summary_of_business_results,net_assets_summary_of_business_results_unit_ref,total_assets_summary_of_business_results,total_assets_summary_of_business_results_unit_ref,number_of_employees,number_of_employees_unit_ref) FROM STDIN WITH (FORMAT csv, NULL '')";
}
