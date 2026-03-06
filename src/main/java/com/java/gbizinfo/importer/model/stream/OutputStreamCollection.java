package com.java.gbizinfo.importer.model.stream;

import lombok.Getter;

import java.io.ByteArrayOutputStream;

@Getter
public class OutputStreamCollection {

    private final ByteArrayOutputStream companyItemBuf;
    private final ByteArrayOutputStream patentBuf;
    private final ByteArrayOutputStream classificationBuf;
    private final ByteArrayOutputStream certificationBuf;
    private final ByteArrayOutputStream subsidyBuf;
    private final ByteArrayOutputStream commendationBuf;
    private final ByteArrayOutputStream procurementBuf;
    private final ByteArrayOutputStream workplaceBuf;
    private final ByteArrayOutputStream baseInfoBuf;
    private final ByteArrayOutputStream womenActivityBuf;
    private final ByteArrayOutputStream compatChildWorkBuf;
    private final ByteArrayOutputStream financeBuf;
    private final ByteArrayOutputStream majorShareholderBuf;
    private final ByteArrayOutputStream managementIndexBuf;

    public OutputStreamCollection() {
        this.companyItemBuf = new ByteArrayOutputStream();
        this.patentBuf = new ByteArrayOutputStream();
        this.classificationBuf = new ByteArrayOutputStream();
        this.certificationBuf = new ByteArrayOutputStream();
        this.subsidyBuf = new ByteArrayOutputStream();
        this.commendationBuf = new ByteArrayOutputStream();
        this.procurementBuf = new ByteArrayOutputStream();
        this.workplaceBuf = new ByteArrayOutputStream();
        this.baseInfoBuf = new ByteArrayOutputStream();
        this.womenActivityBuf = new ByteArrayOutputStream();
        this.compatChildWorkBuf = new ByteArrayOutputStream();
        this.financeBuf = new ByteArrayOutputStream();
        this.majorShareholderBuf = new ByteArrayOutputStream();
        this.managementIndexBuf = new ByteArrayOutputStream();
    }
}