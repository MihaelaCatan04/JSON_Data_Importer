package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompatibilityOfChildcareAndWork {
    @JsonProperty("number_of_paternity_leave")
    private Integer numberOfPaternityLeave;

    @JsonProperty("number_of_maternity_leave")
    private Integer numberOfMaternityLeave;

    @JsonProperty("paternity_leave_acquisition_num")
    private Integer paternityLeaveAcquisitionNum;

    @JsonProperty("maternity_leave_acquisition_num")
    private Integer maternityLeaveAcquisitionNum;
}
