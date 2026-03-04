package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Classifications {
    @JsonProperty("コード値")
    private String codeValue;

    @JsonProperty("コード名")
    private String codeName;

    @JsonProperty("日本語")
    private String japanese;
}
