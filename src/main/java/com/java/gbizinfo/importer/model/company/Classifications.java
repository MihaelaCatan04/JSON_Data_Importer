package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.mergeKey;
import static com.java.gbizinfo.importer.util.HashUtil.normText;

@Setter
@Getter
public class Classifications {
    @JsonProperty("コード値")
    private String codeValue;

    @JsonProperty("コード名")
    private String codeName;

    @JsonProperty("日本語")
    private String japanese;

    public String classificationMergeKey() {
        return mergeKey(normText(this.codeValue), normText(this.codeName), normText(this.japanese));
    }

    public void writeRow(String patentMergeKey) throws IOException {
        String classificationMergeKey = classificationMergeKey();
        StagingBuffer.classification.writeRow(patentMergeKey, classificationMergeKey, this.codeValue, this.codeName, this.japanese);
    }
}
