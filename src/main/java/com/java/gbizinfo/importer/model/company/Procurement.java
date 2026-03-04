package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Procurement {
    @JsonProperty("date_of_order")
    private String dateOfOrder;

    @JsonProperty("title")
    private String title;

    @JsonProperty("amount")
    private Long amount;

    @JsonProperty("government_departments")
    private String governmentDepartments;

    @JsonProperty("note")
    private String note;
}
