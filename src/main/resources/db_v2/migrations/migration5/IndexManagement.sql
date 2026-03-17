CREATE UNIQUE INDEX management_idx
    ON management_index (
                         COALESCE(operating_revenue1_summary_of_business_results::text, ''),
                         COALESCE(operating_revenue1_summary_of_business_results_unit_ref, ''),
                         COALESCE(operating_revenue2_summary_of_business_results::text, ''),
                         COALESCE(operating_revenue2_summary_of_business_results_unit_ref, ''),
                         COALESCE(gross_operating_revenue_summary_of_business_results::text, ''),
                         COALESCE(gross_operating_revenue_summary_of_business_results_unit_ref, ''),
                         COALESCE(ordinary_income_summary_of_business_results::text, ''),
                         COALESCE(ordinary_income_summary_of_business_results_unit_ref, ''),
                         COALESCE(net_premiums_written_summary_of_business_results_ins::text, ''),
                         COALESCE(net_premiums_written_summary_of_business_results_ins_unit_ref, ''),
                         COALESCE(ordinary_income_loss_summary_of_business_results::text, ''),
                         COALESCE(ordinary_income_loss_summary_of_business_results_unit_ref, ''),
                         COALESCE(net_income_loss_summary_of_business_results::text, ''),
                         COALESCE(net_income_loss_summary_of_business_results_unit_ref, ''),
                         COALESCE(capital_stock_summary_of_business_results::text, ''),
                         COALESCE(capital_stock_summary_of_business_results_unit_ref, ''),
                         COALESCE(net_assets_summary_of_business_results::text, ''),
                         COALESCE(net_assets_summary_of_business_results_unit_ref, ''),
                         COALESCE(total_assets_summary_of_business_results::text, ''),
                         COALESCE(total_assets_summary_of_business_results_unit_ref, ''),
                         COALESCE(number_of_employees::text, ''),
                         COALESCE(number_of_employees_unit_ref, '')
        )
    WHERE deleted IS false