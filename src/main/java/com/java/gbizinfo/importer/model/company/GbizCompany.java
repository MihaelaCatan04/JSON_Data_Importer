package com.java.gbizinfo.importer.model.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class GbizCompany {

    @JsonProperty("corporate_number")
    private String corporateNumber;

    @JsonProperty("name")
    private String name;

    @JsonProperty("kana")
    private String kana;

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonProperty("location")
    private String location;

    @JsonProperty("process")
    private String process;

    @JsonProperty("aggregated_year")
    private String aggregatedYear;

    @JsonProperty("status")
    private String status;

    @JsonProperty("close_date")
    private String closeDate;

    @JsonProperty("close_cause")
    private String closeCause;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("representative_name")
    private String representativeName;

    @JsonProperty("capital_stock")
    private Long capitalStock;

    @JsonProperty("employee_number")
    private Integer employeeNumber;

    @JsonProperty("company_size_male")
    private Integer companySizeMale;

    @JsonProperty("company_size_female")
    private Integer companySizeFemale;

    @JsonProperty("business_summary")
    private String businessSummary;

    @JsonProperty("company_url")
    private String companyUrl;

    @JsonProperty("founding_year")
    private Integer foundingYear;

    @JsonProperty("date_of_establishment")
    private String dateOfEstablishment;

    @JsonProperty("qualification_grade")
    private String qualificationGrade;

    @JsonProperty("update_date")
    private String updateDate;

    @JsonProperty("industry")
    private List<String> industry;

    @JsonProperty("business_items")
    private List<String> businessItems;

    @JsonProperty("patent")
    private List<Patent> patent;

    @JsonProperty("certification")
    private List<Certification> certification;

    @JsonProperty("subsidy")
    private List<Subsidy> subsidy;

    @JsonProperty("workplace_info")
    private WorkplaceInfo workplaceInfo;

    @JsonProperty("commendation")
    private List<Commendation> commendation;

    @JsonProperty("procurement")
    private List<Procurement> procurement;

    @JsonProperty("finance")
    private List<Finance> finance;

    @JsonProperty("meta-data")
    private MetaData metaData;

    public void writeRow() throws IOException {
        StagingBuffer.company.writeRow(this.corporateNumber, this.name, this.kana, this.nameEn, this.postalCode, this.location, this.process, this.aggregatedYear, this.status, this.closeDate, this.closeCause, this.kind, this.representativeName, this.capitalStock, this.employeeNumber, this.companySizeMale, this.companySizeFemale, this.businessSummary, this.companyUrl, this.foundingYear, this.dateOfEstablishment, this.qualificationGrade, this.updateDate);
    }

    public void writeIndustry() throws IOException {
        if (industry == null) {
            return;
        }
        for (String value : industry) {
            if (value == null || value.isBlank()) {
                continue;
            }
            StagingBuffer.companyItem.writeRow(corporateNumber, value, true);
        }
    }

    public void writeBusiness() throws IOException {
        if (businessItems == null) {
            return;
        }
        for (String value : businessItems) {
            if (value == null || value.isBlank()) {
                continue;
            }
            StagingBuffer.companyItem.writeRow(corporateNumber, value, false);
        }
    }

    public void writePatent() throws IOException {
        if (patent == null) {
            return;
        }
        for (Patent patent : patent) {
            patent.writePatent(corporateNumber);
        }
    }

    public void writeCertification() throws IOException {
        if (certification == null) {
            return;
        }
        for (Certification certification : certification) {
            certification.writeCertification(corporateNumber);
        }
    }

    public void writeSubsidies() throws IOException {
        if (subsidy == null) {
            return;
        }
        for (Subsidy subsidy : subsidy) {
            subsidy.writeSubsidy(corporateNumber);
        }
    }

    public void writeCommendations() throws IOException {
        if (commendation == null) {
            return;
        }
        for (Commendation commendation : commendation) {
            commendation.writeCommendation(corporateNumber);
        }
    }

    public void writeProcurements() throws IOException {
        if (procurement == null) {
            return;
        }
        for (Procurement procurement : procurement) {
            procurement.writeProcurement(corporateNumber);
        }
    }

    public void writeWorkplaceInfo() throws IOException {
        if (workplaceInfo == null) {
            return;
        }
        BaseInfos baseInfos = workplaceInfo.getBaseInfos();
        WomenActivityInfos womenActivityInfos = workplaceInfo.getWomenActivityInfos();
        CompatibilityOfChildcareAndWork compatibilityOfChildcareAndWork = workplaceInfo.getCompatibilityOfChildcareAndWork();

        if (baseInfos != null) {
            baseInfos.writeBaseInfo(corporateNumber);
        }

        if (womenActivityInfos != null) {
            womenActivityInfos.writeWomenActivityInfo(corporateNumber);
        }

        if (compatibilityOfChildcareAndWork != null) {
            compatibilityOfChildcareAndWork.writeCompatibilityOfChildcareAndWork(corporateNumber);
        }
    }

    public void writeFinances() throws IOException {
        if (finance == null) {
            return;
        }
        for (Finance finance : finance) {
            finance.writeFinance(corporateNumber);
        }
    }
}