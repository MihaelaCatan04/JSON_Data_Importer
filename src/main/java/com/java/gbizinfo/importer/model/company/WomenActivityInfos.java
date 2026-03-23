package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.*;

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

    public String womenActivityMergeKey() {
        return mergeKey(normText(this.femaleWorkersProportionType), normNumber(this.femaleWorkersProportion), normNumber(this.femaleShareOfManager), normNumber(this.genderTotalOfManager), normNumber(this.femaleShareOfOfficers), normNumber(this.genderTotalOfOfficers));
    }

    public void writeWomenActivityInfo(String corporateNumber) throws IOException {
        String mergeKey = womenActivityMergeKey();
        StagingBuffer.womenActivity.writeRow(corporateNumber, mergeKey, this.femaleWorkersProportionType, this.femaleWorkersProportion, this.femaleShareOfManager, this.genderTotalOfManager, this.femaleShareOfOfficers, this.genderTotalOfOfficers);
    }
}
