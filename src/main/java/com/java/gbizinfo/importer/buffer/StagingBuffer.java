package com.java.gbizinfo.importer.buffer;

public class StagingBuffer {

    public final CsvWriter company = new CsvWriter();
    public final CsvWriter companyItem = new CsvWriter();
    public final CsvWriter patent = new CsvWriter();
    public final CsvWriter classification = new CsvWriter();
    public final CsvWriter certification = new CsvWriter();
    public final CsvWriter subsidy = new CsvWriter();
    public final CsvWriter commendation = new CsvWriter();
    public final CsvWriter procurement = new CsvWriter();
    public final CsvWriter baseInfo = new CsvWriter();
    public final CsvWriter womenActivity = new CsvWriter();
    public final CsvWriter compatChildcare = new CsvWriter();
    public final CsvWriter finance = new CsvWriter();
    public final CsvWriter majorShareholder = new CsvWriter();
    public final CsvWriter managementIndex = new CsvWriter();

    public byte[] companyBytes() {
        return company.toByteArray();
    }

    public byte[] companyItemBytes() {
        return companyItem.toByteArray();
    }

    public byte[] patentBytes() {
        return patent.toByteArray();
    }

    public byte[] classificationBytes() {
        return classification.toByteArray();
    }

    public byte[] certificationBytes() {
        return certification.toByteArray();
    }

    public byte[] subsidyBytes() {
        return subsidy.toByteArray();
    }

    public byte[] commendationBytes() {
        return commendation.toByteArray();
    }

    public byte[] procurementBytes() {
        return procurement.toByteArray();
    }

    public byte[] baseInfoBytes() {
        return baseInfo.toByteArray();
    }

    public byte[] womenActivityBytes() {
        return womenActivity.toByteArray();
    }

    public byte[] compatChildcareBytes() {
        return compatChildcare.toByteArray();
    }

    public byte[] financeBytes() {
        return finance.toByteArray();
    }

    public byte[] majorShareholderBytes() {
        return majorShareholder.toByteArray();
    }

    public byte[] managementIndexBytes() {
        return managementIndex.toByteArray();
    }
}