package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static com.java.gbizinfo.importer.util.HashUtil.mergeKey;
import static com.java.gbizinfo.importer.util.HashUtil.normInt;

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

    public String compatChildcareMergeKey() {
        return mergeKey(normInt(this.numberOfPaternityLeave), normInt(this.numberOfMaternityLeave), normInt(this.paternityLeaveAcquisitionNum), normInt(this.maternityLeaveAcquisitionNum));
    }

    public void writeCompatibilityOfChildcareAndWork(String corporateNumber) throws IOException {
        String mergeKey = compatChildcareMergeKey();
        StagingBuffer.compatChildcare.writeRow(corporateNumber, mergeKey, this.numberOfPaternityLeave, this.numberOfMaternityLeave, this.paternityLeaveAcquisitionNum, this.maternityLeaveAcquisitionNum);

    }
}
