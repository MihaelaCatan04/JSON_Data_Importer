package com.java.gbizinfo.importer.model.writer;

import com.java.gbizinfo.importer.model.stream.OutputStreamCollection;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Getter
public class WriterCollection {
    private final BufferedWriter companyItemWriter;
    private final BufferedWriter patentWriter;
    private final BufferedWriter classificationWriter;
    private final BufferedWriter certificationWriter;
    private final BufferedWriter subsidyWriter;
    private final BufferedWriter commendationWriter;
    private final BufferedWriter procurementWriter;
    private final BufferedWriter workplaceWriter;
    private final BufferedWriter baseInfoWriter;
    private final BufferedWriter womenActivityWriter;
    private final BufferedWriter compatChildWorkWriter;
    private final BufferedWriter financeWriter;
    private final BufferedWriter majorShareholderWriter;
    private final BufferedWriter managementIndexWriter;

    public WriterCollection(OutputStreamCollection s) {
        this.companyItemWriter = getWriter(s.getCompanyItemBuf());
        this.patentWriter = getWriter(s.getPatentBuf());
        this.classificationWriter = getWriter(s.getClassificationBuf());
        this.certificationWriter = getWriter(s.getCertificationBuf());
        this.subsidyWriter = getWriter(s.getSubsidyBuf());
        this.commendationWriter = getWriter(s.getCommendationBuf());
        this.procurementWriter = getWriter(s.getProcurementBuf());
        this.workplaceWriter = getWriter(s.getWorkplaceBuf());
        this.baseInfoWriter = getWriter(s.getBaseInfoBuf());
        this.womenActivityWriter = getWriter(s.getWomenActivityBuf());
        this.compatChildWorkWriter = getWriter(s.getCompatChildWorkBuf());
        this.financeWriter = getWriter(s.getFinanceBuf());
        this.majorShareholderWriter = getWriter(s.getMajorShareholderBuf());
        this.managementIndexWriter = getWriter(s.getManagementIndexBuf());
    }

    private BufferedWriter getWriter(OutputStream element) {
        return new BufferedWriter(new OutputStreamWriter(element, StandardCharsets.UTF_8));
    }

    public void flushAll() throws IOException {
        companyItemWriter.flush();
        patentWriter.flush();
        classificationWriter.flush();
        certificationWriter.flush();
        subsidyWriter.flush();
        commendationWriter.flush();
        procurementWriter.flush();
        workplaceWriter.flush();
        baseInfoWriter.flush();
        womenActivityWriter.flush();
        compatChildWorkWriter.flush();
        financeWriter.flush();
        majorShareholderWriter.flush();
        managementIndexWriter.flush();
    }
}