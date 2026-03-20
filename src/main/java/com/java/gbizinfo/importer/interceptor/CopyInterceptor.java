package com.java.gbizinfo.importer.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.model.company.*;
import com.java.gbizinfo.importer.util.CopyUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.zip.ZipInputStream;

@Log4j2
@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class CopyInterceptor implements Interceptor {


    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if (!CopyUtil.COPY_METHOD.equals(ms.getId())) {
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();
        Connection connection = executor.getTransaction().getConnection();
        PGConnection pgConnection = connection.unwrap(PGConnection.class);

        ZipInputStream zipInputStream = CopyContext.getZip();
        if (zipInputStream == null) {
            throw new IllegalStateException("CopyContext zip stream is not set");
        }

        executeCopy(pgConnection, zipInputStream);
        return 1;
    }

    private void executeCopy(PGConnection pg, ZipInputStream zip) throws Exception {
        StagingBuffer buffer = new StagingBuffer();
        streamIntoBuffer(zip, buffer);

        copyToStaging(pg, CopyUtil.COPY_STG_COMPANY, buffer.companyBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_COMPANY_ITEM, buffer.companyItemBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_PATENT, buffer.patentBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_CLASSIFICATION, buffer.classificationBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_CERTIFICATION, buffer.certificationBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_SUBSIDY, buffer.subsidyBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_COMMENDATION, buffer.commendationBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_PROCUREMENT, buffer.procurementBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_BASE_INFO, buffer.baseInfoBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_WOMEN_ACTIVITY, buffer.womenActivityBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_COMPAT_CHILDCARE, buffer.compatChildcareBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_FINANCE, buffer.financeBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_MAJOR_SHAREHOLDER, buffer.majorShareholderBytes());
        copyToStaging(pg, CopyUtil.COPY_STG_MANAGEMENT_INDEX, buffer.managementIndexBytes());

        log.info("COPY into staging completed");
    }

    private void streamIntoBuffer(ZipInputStream zip, StagingBuffer buffer) throws Exception {
        InputStream nonClosing = new FilterInputStream(zip) {
            @Override
            public void close() {
                // zip entry is managed outside
            }
        };

        try (MappingIterator<GbizCompany> iterator = MAPPER.readerFor(GbizCompany.class).readValues(nonClosing)) {
            while (iterator.hasNextValue()) {
                GbizCompany company = iterator.nextValue();
                writeCompany(buffer, company);
                writeCompanyItems(buffer, company);
                writePatents(buffer, company);
                writeCertifications(buffer, company);
                writeSubsidies(buffer, company);
                writeCommendations(buffer, company);
                writeProcurements(buffer, company);
                writeWorkplaceInfo(buffer, company);
                writeFinances(buffer, company);
            }
        }
    }

    private void writeCompany(StagingBuffer buffer, GbizCompany company) throws IOException {
        buffer.company.writeRow(company.getCorporateNumber(), company.getName(), company.getKana(), company.getNameEn(), company.getPostalCode(), company.getLocation(), company.getProcess(), company.getAggregatedYear(), company.getStatus(), company.getCloseDate(), company.getCloseCause(), company.getKind(), company.getRepresentativeName(), company.getCapitalStock(), company.getEmployeeNumber(), company.getCompanySizeMale(), company.getCompanySizeFemale(), company.getBusinessSummary(), company.getCompanyUrl(), company.getFoundingYear(), company.getDateOfEstablishment(), company.getQualificationGrade(), company.getUpdateDate());
    }

    private void writeCompanyItems(StagingBuffer buffer, GbizCompany company) throws IOException {
        writeIndustry(buffer, company);
        writeBusiness(buffer, company);
    }

    private void writeIndustry(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getIndustry() != null) {
            for (String value : company.getIndustry()) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                buffer.companyItem.writeRow(company.getCorporateNumber(), value, true);
            }
        }
    }

    private void writeBusiness(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getBusinessItems() != null) {
            for (String value : company.getBusinessItems()) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                buffer.companyItem.writeRow(company.getCorporateNumber(), value, false);
            }
        }
    }

    private void writePatents(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getPatent() == null) {
            return;
        }

        for (Patent patent : company.getPatent()) {
            String patentMergeKey = patent.patentMergeKey();

            buffer.patent.writeRow(company.getCorporateNumber(), patentMergeKey, patent.getPatentType(), patent.getRegistrationNumber(), patent.getApplicationDate(), patent.getTitle(), patent.getUrl());

            writeClassification(buffer, patent, patentMergeKey);
        }
    }

    private void writeClassification(StagingBuffer buffer, Patent patent, String patentMergeKey) throws IOException {
        if (patent.getClassifications() != null) {
            for (Classifications cl : patent.getClassifications()) {
                String classificationMergeKey = cl.classificationMergeKey();

                buffer.classification.writeRow(patentMergeKey, classificationMergeKey, cl.getCodeValue(), cl.getCodeName(), cl.getJapanese());
            }
        }
    }

    private void writeCertifications(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getCertification() == null) {
            return;
        }

        for (Certification certification : company.getCertification()) {
            String mergeKey = certification.certificationMergeKey();

            buffer.certification.writeRow(company.getCorporateNumber(), mergeKey, certification.getDateOfApproval(), certification.getTitle(), certification.getTarget(), certification.getGovernmentDepartments(), certification.getCategory());
        }
    }

    private void writeSubsidies(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getSubsidy() == null) {
            return;
        }

        for (Subsidy subsidy : company.getSubsidy()) {
            String mergeKey = subsidy.subsidyMergeKey();

            buffer.subsidy.writeRow(company.getCorporateNumber(), mergeKey, subsidy.getDateOfApproval(), subsidy.getTitle(), subsidy.getAmount(), subsidy.getTarget(), subsidy.getGovernmentDepartments());
        }
    }

    private void writeCommendations(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getCommendation() == null) {
            return;
        }

        for (Commendation commendation : company.getCommendation()) {
            String mergeKey = commendation.commendationMergeKey();

            buffer.commendation.writeRow(company.getCorporateNumber(), mergeKey, commendation.getDateOfCommendation(), commendation.getTitle(), commendation.getTarget(), commendation.getCategory(), commendation.getGovernmentDepartments(), commendation.getNote());
        }
    }

    private void writeProcurements(StagingBuffer buffer, GbizCompany company) throws IOException {
        if (company.getProcurement() == null) {
            return;
        }

        for (Procurement procurement : company.getProcurement()) {
            String mergeKey = procurement.procurementMergeKey();

            buffer.procurement.writeRow(company.getCorporateNumber(), mergeKey, procurement.getDateOfOrder(), procurement.getTitle(), procurement.getAmount(), procurement.getGovernmentDepartments(), procurement.getNote());
        }
    }

    private void writeWorkplaceInfo(StagingBuffer buffer, GbizCompany company) throws IOException {
        WorkplaceInfo workplaceInfo = company.getWorkplaceInfo();
        if (workplaceInfo == null) {
            return;
        }

        writeBaseInfo(buffer, workplaceInfo, company);

        writeWomenActivityInfo(buffer, workplaceInfo, company);

        writeCompatibilityOfChildcareAndWork(buffer, workplaceInfo, company);
    }

    private void writeBaseInfo(StagingBuffer buffer, WorkplaceInfo workplaceInfo, GbizCompany company) throws IOException {
        BaseInfos base = workplaceInfo.getBaseInfos();
        if (base != null) {
            String mergeKey = base.baseInfoMergeKey();

            buffer.baseInfo.writeRow(company.getCorporateNumber(), mergeKey, base.getAverageContinuousServiceYearsType(), base.getAverageContinuousServiceYearsMale(), base.getAverageContinuousServiceYearsFemale(), base.getAverageContinuousServiceYears(), base.getAverageAge(), base.getMonthAveragePredeterminedOvertimeHours());
        }
    }

    private void writeWomenActivityInfo(StagingBuffer buffer, WorkplaceInfo workplaceInfo, GbizCompany company) throws IOException {
        WomenActivityInfos women = workplaceInfo.getWomenActivityInfos();
        if (women != null) {
            String mergeKey = women.womenActivityMergeKey();

            buffer.womenActivity.writeRow(company.getCorporateNumber(), mergeKey, women.getFemaleWorkersProportionType(), women.getFemaleWorkersProportion(), women.getFemaleShareOfManager(), women.getGenderTotalOfManager(), women.getFemaleShareOfOfficers(), women.getGenderTotalOfOfficers());
        }
    }

    private void writeCompatibilityOfChildcareAndWork(StagingBuffer buffer, WorkplaceInfo workplaceInfo, GbizCompany company) throws IOException {
        CompatibilityOfChildcareAndWork compat = workplaceInfo.getCompatibilityOfChildcareAndWork();
        if (compat != null) {
            String mergeKey = compat.compatChildcareMergeKey();

            buffer.compatChildcare.writeRow(company.getCorporateNumber(), mergeKey, compat.getNumberOfPaternityLeave(), compat.getNumberOfMaternityLeave(), compat.getPaternityLeaveAcquisitionNum(), compat.getMaternityLeaveAcquisitionNum());
        }
    }

    private void writeFinances(StagingBuffer buffer, GbizCompany company) throws IOException {
        List<Finance> finances = company.getFinance();
        if (finances == null) {
            return;
        }

        for (Finance finance : finances) {
            String financeMergeKey = finance.financeMergeKey();

            buffer.finance.writeRow(company.getCorporateNumber(), financeMergeKey, finance.getAccountingStandards(), finance.getFiscalYearCoverPage());

            writeMajorShareholders(buffer, finance, financeMergeKey);
            writeManagementIndexes(buffer, finance, financeMergeKey);
        }
    }

    private void writeMajorShareholders(StagingBuffer buffer, Finance finance, String financeMergeKey) throws IOException {
        if (finance.getMajorShareholders() == null) {
            return;
        }

        for (MajorShareholders shareholder : finance.getMajorShareholders()) {
            String mergeKey = shareholder.shareholderMergeKey();

            buffer.majorShareholder.writeRow(financeMergeKey, mergeKey, shareholder.getNameMajorShareholders(), shareholder.getShareholdingRatio());
        }
    }

    private void writeManagementIndexes(StagingBuffer buffer, Finance finance, String financeMergeKey) throws IOException {
        if (finance.getManagementIndex() == null) {
            return;
        }

        for (ManagementIndex index : finance.getManagementIndex()) {
            String mergeKey = index.managementIndexMergeKey();

            buffer.managementIndex.writeRow(financeMergeKey, mergeKey, index.getPeriod(), index.getNetSalesSummaryOfBusinessResults(), index.getNetSalesSummaryOfBusinessResultsUnitRef(), index.getOperatingRevenue1SummaryOfBusinessResults(), index.getOperatingRevenue1SummaryOfBusinessResultsUnitRef(), index.getOperatingRevenue2SummaryOfBusinessResults(), index.getOperatingRevenue2SummaryOfBusinessResultsUnitRef(), index.getGrossOperatingRevenueSummaryOfBusinessResults(), index.getGrossOperatingRevenueSummaryOfBusinessResultsUnitRef(), index.getOrdinaryIncomeSummaryOfBusinessResults(), index.getOrdinaryIncomeSummaryOfBusinessResultsUnitRef(), index.getNetPremiumsWrittenSummaryOfBusinessResultIns(), index.getNetPremiumsWrittenSummaryOfBusinessResultsInsUnitRef(), index.getOrdinaryIncomeLossSummaryOfBusinessResults(), index.getOrdinaryIncomeLossSummaryOfBusinessResultsUnitRef(), index.getNetIncomeLossSummaryOfBusinessResults(), index.getNetIncomeLossSummaryOfBusinessResultsUnitRef(), index.getCapitalStockSummaryOfBusinessResults(), index.getCapitalStockSummaryOfBusinessResultsUnitRef(), index.getNetAssetsSummaryOfBusinessResults(), index.getNetAssetsSummaryOfBusinessResultsUnitRef(), index.getTotalAssetsSummaryOfBusinessResults(), index.getTotalAssetsSummaryOfBusinessResultsUnitRef(), index.getNumberOfEmployees(), index.getNumberOfEmployeesUnitRef());
        }
    }

    private void copyToStaging(PGConnection pg, String sql, byte[] data) throws Exception {
        if (data.length == 0) {
            return;
        }

        try (PGCopyOutputStream out = new PGCopyOutputStream(pg, sql)) {
            out.write(data);
        }

        log.debug("Copied {} bytes into staging using {}", data.length, sql);
    }
}