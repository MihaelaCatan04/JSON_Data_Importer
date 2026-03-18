package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Finance {
    @JsonProperty("accounting_standards")
    private String accountingStandards;

    @JsonProperty("fiscal_year_cover_page")
    private String fiscalYearCoverPage;

    @JsonProperty("management_index")
    private List<ManagementIndex> managementIndex;

    @JsonProperty("major_shareholders")
    private List<MajorShareholders> majorShareholders;
}