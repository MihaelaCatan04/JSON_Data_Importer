package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.*;

@Setter
@Getter
public class Subsidy {
    @JsonProperty("date_of_approval")
    private String dateOfApproval;

    @JsonProperty("title")
    private String title;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("target")
    private String target;

    @JsonProperty("government_departments")
    private String governmentDepartments;

    public String subsidyMergeKey() {
        return mergeKey(normDate(this.dateOfApproval), normText(this.title), normText(this.amount), normText(this.target), normText(this.governmentDepartments));
    }

    public void writeSubsidy(String corporateNumber) throws IOException {
        String mergeKey = subsidyMergeKey();
        StagingBuffer.subsidy.writeRow(corporateNumber, mergeKey, this.dateOfApproval, this.title, this.amount, this.target, this.governmentDepartments);
    }
}