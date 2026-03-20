package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.*;

@Setter
@Getter
public class BaseInfos {
    @JsonProperty("average_continuous_service_years_type")
    private String averageContinuousServiceYearsType;

    @JsonProperty("average_continuous_service_years_Male")
    private Double averageContinuousServiceYearsMale;

    @JsonProperty("average_continuous_service_years_Female")
    private Double averageContinuousServiceYearsFemale;

    @JsonProperty("average_continuous_service_years")
    private Double averageContinuousServiceYears;

    @JsonProperty("average_age")
    private Double averageAge;

    @JsonProperty("month_average_predetermined_overtime_hours")
    private Double monthAveragePredeterminedOvertimeHours;

    public String baseInfoMergeKey() {
        return mergeKey(normText(this.averageContinuousServiceYearsType), normNumber(this.averageContinuousServiceYearsMale), normNumber(averageContinuousServiceYearsFemale), normNumber(averageContinuousServiceYears), normNumber(averageAge), normNumber(monthAveragePredeterminedOvertimeHours));
    }

    public void writeBaseInfo(String corporateNumber) throws IOException {
        String mergeKey = baseInfoMergeKey();
        StagingBuffer.baseInfo.writeRow(corporateNumber, mergeKey, this.averageContinuousServiceYearsType, this.averageContinuousServiceYearsMale, this.averageContinuousServiceYearsFemale, this.averageContinuousServiceYears, this.averageAge, this.monthAveragePredeterminedOvertimeHours);
    }
}
