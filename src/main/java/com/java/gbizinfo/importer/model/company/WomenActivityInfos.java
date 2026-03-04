package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WomenActivityInfos {
    @JsonProperty("female_workers_proportion_type")
    private String femaleWorkersProportionType;

    @JsonProperty("female_workers_proportion")
    private Double femaleWorkersProportion;

    @JsonProperty("female_share_of_manager")
    private Integer femaleShareOfManager;

    @JsonProperty("gender_total_of_manager")
    private Integer genderTotalOfManager;

    @JsonProperty("female_share_of_officers")
    private Integer femaleShareOfOfficers;

    @JsonProperty("gender_total_of_officers")
    private Integer genderTotalOfOfficers;
}
