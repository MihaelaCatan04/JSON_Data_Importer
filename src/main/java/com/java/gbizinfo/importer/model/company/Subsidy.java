package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Subsidy {
    @JsonProperty("date_of_approval")
    private String dateOfApproval;

    @JsonProperty("title")
    private String title;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("target")
    private String target;

    @JsonProperty("government_departments")
    private String governmentDepartments;
}