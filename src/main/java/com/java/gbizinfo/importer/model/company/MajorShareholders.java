package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MajorShareholders {
    @JsonProperty("name_major_shareholders")
    private String nameMajorShareholders;

    @JsonProperty("shareholding_ratio")
    private Double shareholdingRatio;
}
