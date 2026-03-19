package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Commendation {
    @JsonProperty("date_of_commendation")
    private String dateOfCommendation;

    @JsonProperty("title")
    private String title;

    @JsonProperty("target")
    private String target;

    @JsonProperty("category")
    private String category;

    @JsonProperty("government_departments")
    private String governmentDepartments;

    @JsonProperty("note")
    private String note;
}