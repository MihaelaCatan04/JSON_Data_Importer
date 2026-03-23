package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.*;

@Setter
@Getter
public class MajorShareholders {
    @JsonProperty("name_major_shareholders")
    private String nameMajorShareholders;

    @JsonProperty("shareholding_ratio")
    private Double shareholdingRatio;

    public String shareholderMergeKey() {
        return mergeKey(normText(this.nameMajorShareholders), normNumber(this.shareholdingRatio));
    }

    public void writeMajorShareholder(String financeMergeKey) throws IOException {
        String mergeKey = shareholderMergeKey();
        StagingBuffer.majorShareholder.writeRow(financeMergeKey, mergeKey, this.nameMajorShareholders, this.shareholdingRatio);
    }
}
