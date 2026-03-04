package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Patent {

    @JsonProperty("patent_type")
    private String patentType;

    @JsonProperty("registration_number")
    private String registrationNumber;

    @JsonProperty("application_date")
    private String applicationDate;

    @JsonProperty("classifications")
    private List<Classifications> classifications;

    @JsonProperty("title")
    private String title;

    @JsonProperty("url")
    private String url;
}
