package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

import static com.java.gbizinfo.importer.util.HashUtil.*;

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

    public String patentMergeKey() {
        return mergeKey(normText(this.patentType), normText(this.registrationNumber), normDate(this.applicationDate), normText(this.title), normText(this.url));
    }

    public void writePatent(String corporateNumber) throws IOException {
        String patentMergeKey = patentMergeKey();
        StagingBuffer.patent.writeRow(corporateNumber, patentMergeKey, this.patentType, this.registrationNumber, this.applicationDate, this.title, this.url);
        writeClassifications();
    }

    public void writeClassifications() throws IOException {
        if (classifications == null) {
            return;
        }
        for (Classifications classifications : classifications) {
            String patentMergeKey = patentMergeKey();
            classifications.writeRow(patentMergeKey);
        }
    }
}