package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

import static com.java.gbizinfo.importer.util.HashUtil.mergeKey;
import static com.java.gbizinfo.importer.util.HashUtil.normText;

@Setter
@Getter
public class Finance {
    @JsonProperty("accounting_standards")
    private String accountingStandards;

    @JsonProperty("fiscal_year_cover_page")
    private String fiscalYearCoverPage;

    @JsonProperty("management_index")
    private List<ManagementIndex> managementIndex;

    @JsonProperty("major_shareholders")
    private List<MajorShareholders> majorShareholders;

    public String financeMergeKey() {
        return mergeKey(normText(this.accountingStandards), normText(this.fiscalYearCoverPage));
    }

    public void writeFinance(String corporateNumber) throws IOException {
        String financeMergeKey = financeMergeKey();
        StagingBuffer.finance.writeRow(corporateNumber, financeMergeKey, this.accountingStandards, this.fiscalYearCoverPage);
        writeMajorShareholder();
        writeManagementIndexes();
    }

    public void writeMajorShareholder() throws IOException {
        if (majorShareholders == null) {
            return;
        }
        for (MajorShareholders majorShareholder : majorShareholders) {
            String financeMergeKey = financeMergeKey();
            majorShareholder.writeMajorShareholder(financeMergeKey);
        }
    }

    public void writeManagementIndexes() throws IOException {
        if (managementIndex == null) {
            return;
        }
        for (ManagementIndex managementIndex : managementIndex) {
            String financeMergeKey = financeMergeKey();
            managementIndex.writeManagementIndex(financeMergeKey);
        }
    }
}