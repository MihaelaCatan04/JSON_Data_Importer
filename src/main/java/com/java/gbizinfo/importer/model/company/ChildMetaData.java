package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildMetaData {
    @JsonProperty("last_update_date")
    private String lastUpdateDate;
}