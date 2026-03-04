package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
}
