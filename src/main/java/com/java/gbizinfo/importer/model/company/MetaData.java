package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MetaData {
    @JsonProperty("last_update_date")
    private Map<String, String> lastUpdateDate;
}
