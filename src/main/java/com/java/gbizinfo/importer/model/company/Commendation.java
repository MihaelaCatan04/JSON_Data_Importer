package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.*;

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

    public String commendationMergeKey() {
        return mergeKey(normDate(this.dateOfCommendation), normText(this.title), normText(this.target), normText(this.category), normText(this.governmentDepartments), normText(this.note));
    }

    public void writeCommendation(String corporateNumber) throws IOException {
        String mergeKey = commendationMergeKey();
        StagingBuffer.commendation.writeRow(corporateNumber, mergeKey, this.dateOfCommendation, this.title, this.target, this.category, this.governmentDepartments, this.note);
    }
}