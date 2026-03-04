package com.java.gbizinfo.importer.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.model.company.*;
import com.java.gbizinfo.importer.model.stream.OutputStreamCollection;
import com.java.gbizinfo.importer.model.writer.WriterCollection;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipInputStream;

@Log4j2
@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class CopyInterceptor implements Interceptor {

    private static final String COPY_METHOD = "com.java.gbizinfo.importer.mapper.DataMapper.copy";

    private static final String COMPANY_COPY = """
            COPY company (
                corporate_number, name, kana, name_en, postal_code, location,
                process, aggregated_year, status, close_date, close_status, kind,
                representative_name, capital_stock, employee_number,
                company_size_male, company_size_female, business_summary,
                company_url, founding_year, date_of_establishment,
                qualification_grade, update_date
            ) FROM STDIN WITH (FORMAT csv, NULL '')
            """;

    private static final String INDUSTRY_COPY = "COPY industry (corporate_number, industry_type) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String BUSINESS_ITEM_COPY = "COPY business_item (corporate_number, item_type) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String PATENT_COPY = "COPY patent (patent_id, corporate_number, patent_type, registration_number, application_date, title, url) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String CLASSIFICATION_COPY = "COPY classification (patent_id, code_value, code_name, japanese) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String CERTIFICATION_COPY = "COPY certification (corporate_number, date_of_approval, title, target, government_departments, category) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String SUBSIDY_COPY = "COPY subsidy (corporate_number, date_of_approval, title, amount, target, government_departments) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String WORKPLACE_INFO_COPY = "COPY workplace_info (workplace_info_id, corporate_number) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String BASE_INFO_COPY = "COPY base_info (workplace_info_id, average_continuous_service_years_type, average_continuous_service_years_male, average_continuous_service_years_female, average_continuous_service_years, average_age, month_average_predetermined_overtime_hours) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String WOMEN_ACTIVITY_INFO_COPY = "COPY women_activity_info (workplace_info_id, female_workers_proportion_type, female_workers_proportion, female_share_of_manager, gender_total_of_manager, female_share_of_officers, gender_total_of_officers) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String COMPATIBILITY_OF_CHILDCARE_AND_WORK_COPY = "COPY compatibility_of_childcare_and_work (workplace_info_id, number_of_paternity_leave, number_of_maternity_leave, paternity_leave_acquisition_num, maternity_leave_acquisition_num) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String COMMENDATION_COPY = "COPY commendation (corporate_number, date_of_commendation, title, target, category, government_departments, note) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String PROCUREMENT_COPY = "COPY procurement (corporate_number, date_of_order, title, amount, government_departments, note) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String FINANCE_COPY = "COPY finance (finance_id, corporate_number, accounting_standards, fiscal_year_cover_page) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String MAJOR_SHAREHOLDER_COPY = "COPY major_shareholder (finance_id, name_major_stakeholders, shareholding_ratio) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String MANAGEMENT_INDEX_COPY = "COPY management_index (finance_id, period, net_sales_summary_of_business_results, net_sales_summary_of_business_results_unit_ref, operating_revenue1_summary_of_business_results, operating_revenue1_summary_of_business_results_unit_ref, operating_revenue2_summary_of_business_results, operating_revenue2_summary_of_business_results_unit_ref, gross_operating_revenue_summary_of_business_results, gross_operating_revenue_summary_of_business_results_unit_ref, ordinary_income_summary_of_business_results, ordinary_income_summary_of_business_results_unit_ref, net_premiums_written_summary_of_business_results_ins, net_premiums_written_summary_of_business_results_ins_unit_ref, ordinary_income_loss_summary_of_business_results, ordinary_income_loss_summary_of_business_results_unit_ref, net_income_loss_summary_of_business_results, net_income_loss_summary_of_business_results_unit_ref, capital_stock_summary_of_business_results, capital_stock_summary_of_business_results_unit_ref, net_assets_summary_of_business_results, net_assets_summary_of_business_results_unit_ref, total_assets_summary_of_business_results, total_assets_summary_of_business_results_unit_ref, number_of_employees, number_of_employees_unit_ref) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    private final AtomicLong patentIdSeq = new AtomicLong();
    private final AtomicLong workplaceIdSeq = new AtomicLong();
    private final AtomicLong financeIdSeq = new AtomicLong();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if (!COPY_METHOD.equals(ms.getId())) {
            return invocation.proceed();
        }

        Executor executor = (Executor) invocation.getTarget();
        Connection conn = executor.getTransaction().getConnection();
        PGConnection pgConn = conn.unwrap(PGConnection.class);

        ZipInputStream zip = CopyContext.getZip();
        if (zip == null) {
            throw new IllegalStateException("CopyContext ZipInputStream not set before calling copy()");
        }

        initSequences();

        executeCopy(pgConn, zip);
        return 1;
    }

    private void initSequences() {
        patentIdSeq.set(CopyContext.getPatentSeq() + 1);
        workplaceIdSeq.set(CopyContext.getWorkplaceSeq() + 1);
        financeIdSeq.set(CopyContext.getFinanceSeq() + 1);
    }

    private void executeCopy(PGConnection pgConn, ZipInputStream zip) throws Exception {
        OutputStreamCollection streams = new OutputStreamCollection();
        WriterCollection writers = new WriterCollection(streams);

        Long count = copyCompany(pgConn, zip, writers);
        log.info("company COPY done ({} records). Writing child tables.", count);

        writers.flushAll();
        copyBufferedStreams(pgConn, streams);

        log.info("All COPY operations complete.");
    }

    private Long copyCompany(PGConnection pgConn, ZipInputStream zip, WriterCollection writers) throws Exception {
        try (PGCopyOutputStream compStream = new PGCopyOutputStream(pgConn, COMPANY_COPY); BufferedWriter compWriter = createWriter(compStream)) {

            return processZip(zip, compWriter, writers);
        }
    }

    private void copyBufferedStreams(PGConnection pgConn, OutputStreamCollection outputStreamCollection) throws Exception {
        writeBuffer(pgConn, INDUSTRY_COPY, outputStreamCollection.getIndBuf());
        writeBuffer(pgConn, BUSINESS_ITEM_COPY, outputStreamCollection.getItemBuf());

        writeBuffer(pgConn, PATENT_COPY, outputStreamCollection.getPatentBuf());
        writeBuffer(pgConn, CLASSIFICATION_COPY, outputStreamCollection.getClassificationBuf());

        writeBuffer(pgConn, CERTIFICATION_COPY, outputStreamCollection.getCertificationBuf());
        writeBuffer(pgConn, SUBSIDY_COPY, outputStreamCollection.getSubsidyBuf());
        writeBuffer(pgConn, COMMENDATION_COPY, outputStreamCollection.getCommendationBuf());
        writeBuffer(pgConn, PROCUREMENT_COPY, outputStreamCollection.getProcurementBuf());

        writeBuffer(pgConn, WORKPLACE_INFO_COPY, outputStreamCollection.getWorkplaceBuf());
        writeBuffer(pgConn, BASE_INFO_COPY, outputStreamCollection.getBaseInfoBuf());
        writeBuffer(pgConn, WOMEN_ACTIVITY_INFO_COPY, outputStreamCollection.getWomenActivityBuf());
        writeBuffer(pgConn, COMPATIBILITY_OF_CHILDCARE_AND_WORK_COPY, outputStreamCollection.getCompatChildWorkBuf());

        writeBuffer(pgConn, FINANCE_COPY, outputStreamCollection.getFinanceBuf());
        writeBuffer(pgConn, MAJOR_SHAREHOLDER_COPY, outputStreamCollection.getMajorShareholderBuf());
        writeBuffer(pgConn, MANAGEMENT_INDEX_COPY, outputStreamCollection.getManagementIndexBuf());
    }

    private void writeBuffer(PGConnection pgConn, String sql, ByteArrayOutputStream buf) throws Exception {
        byte[] data = buf.toByteArray();
        if (data.length == 0) return;
        try (PGCopyOutputStream out = new PGCopyOutputStream(pgConn, sql)) {
            out.write(data);
        }
    }

    private Long processZip(ZipInputStream zip, BufferedWriter compOut, WriterCollection writerCollection) throws IOException {
        InputStream nonClosing = new FilterInputStream(zip) {
            @Override
            public void close() {
            }
        };
        try (MappingIterator<GbizCompany> it = MAPPER.readerFor(GbizCompany.class).readValues(nonClosing)) {
            return processCompanies(it, compOut, writerCollection);
        }
    }

    private Long processCompanies(MappingIterator<GbizCompany> it, BufferedWriter compOut, WriterCollection writerCollection) throws IOException {
        Long count = 0L;
        while (it.hasNext()) {
            processSingleCompany(it.next(), compOut, writerCollection);
            count++;
        }
        return count;
    }

    private void processSingleCompany(GbizCompany company, BufferedWriter compOut, WriterCollection writerCollection) throws IOException {
        writeCompanyRow(compOut, company);
        writeStringList(writerCollection.getIndWriter(), company.getCorporateNumber(), company.getIndustry());
        writeStringList(writerCollection.getItemWriter(), company.getCorporateNumber(), company.getBusinessItems());
        writePatents(writerCollection, company);
        writeCertifications(writerCollection, company);
        writeSubsidies(writerCollection, company);
        writeCommendations(writerCollection, company);
        writeProcurements(writerCollection, company);
        writeWorkplaceInfos(writerCollection, company);
        writeFinances(writerCollection, company);
    }

    private void writeCompanyRow(BufferedWriter out, GbizCompany company) throws IOException {
        out.write(String.join(",", csv(company.getCorporateNumber()), csv(company.getName()), csv(company.getKana()), csv(company.getNameEn()), csv(company.getPostalCode()), csv(company.getLocation()), csv(company.getProcess()), csv(company.getAggregatedYear()), csv(company.getStatus()), csv(company.getCloseDate()), csv(company.getCloseCause()), csv(company.getKind()), csv(company.getRepresentativeName()), csv(company.getCapitalStock()), csv(company.getEmployeeNumber()), csv(company.getCompanySizeMale()), csv(company.getCompanySizeFemale()), csv(company.getBusinessSummary()), csv(company.getCompanyUrl()), csv(company.getFoundingYear()), csv(company.getDateOfEstablishment()), csv(company.getQualificationGrade()), csv(company.getUpdateDate())));
        out.newLine();
    }

    private void writePatents(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getPatent() == null) return;
        for (Patent patent : company.getPatent()) {
            Long patentId = patentIdSeq.getAndIncrement();
            writerCollection.getPatentWriter().write(String.join(",", csv(patentId), csv(company.getCorporateNumber()), csv(patent.getPatentType()), csv(patent.getRegistrationNumber()), csv(patent.getApplicationDate()), csv(patent.getTitle()), csv(patent.getUrl())));
            writerCollection.getPatentWriter().newLine();

            writeClassifications(writerCollection, patentId, patent.getClassifications());
        }
    }

    private void writeClassifications(WriterCollection writerCollection, Long patentId, List<Classifications> list) throws IOException {
        if (list == null) return;
        for (Classifications classifications : list) {
            writerCollection.getClassificationWriter().write(String.join(",", csv(patentId), csv(classifications.getCodeValue()), csv(classifications.getCodeName()), csv(classifications.getJapanese())));
            writerCollection.getClassificationWriter().newLine();
        }
    }

    private void writeCertifications(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getCertification() == null) return;
        for (Certification certification : company.getCertification()) {
            writerCollection.getCertificationWriter().write(String.join(",", csv(company.getCorporateNumber()), csv(certification.getDateOfApproval()), csv(certification.getTitle()), csv(certification.getTarget()), csv(certification.getGovernmentDepartments()), csv(certification.getCategory())));
            writerCollection.getCertificationWriter().newLine();
        }
    }

    private void writeSubsidies(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getSubsidy() == null) return;
        for (Subsidy subsidy : company.getSubsidy()) {
            writerCollection.getSubsidyWriter().write(String.join(",", csv(company.getCorporateNumber()), csv(subsidy.getDateOfApproval()), csv(subsidy.getTitle()), csv(subsidy.getAmount()), csv(subsidy.getTarget()), csv(subsidy.getGovernmentDepartments())));
            writerCollection.getSubsidyWriter().newLine();
        }
    }

    private void writeCommendations(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getCommendation() == null) return;
        for (Commendation commendation : company.getCommendation()) {
            writerCollection.getCommendationWriter().write(String.join(",", csv(company.getCorporateNumber()), csv(commendation.getDateOfCommendation()), csv(commendation.getTitle()), csv(commendation.getTarget()), csv(commendation.getCategory()), csv(commendation.getGovernmentDepartments()), csv(commendation.getNote())));
            writerCollection.getCommendationWriter().newLine();
        }
    }

    private void writeProcurements(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getProcurement() == null) return;
        for (Procurement procurement : company.getProcurement()) {
            writerCollection.getProcurementWriter().write(String.join(",", csv(company.getCorporateNumber()), csv(procurement.getDateOfOrder()), csv(procurement.getTitle()), csv(procurement.getAmount()), csv(procurement.getGovernmentDepartments()), csv(procurement.getNote())));
            writerCollection.getProcurementWriter().newLine();
        }
    }

    private void writeWorkplaceInfos(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getWorkplaceInfo() == null) return;
        for (WorkplaceInfo workplaceInfo : company.getWorkplaceInfo()) {
            Long workplaceId = workplaceIdSeq.getAndIncrement();
            writeWorkplaceInfoParent(writerCollection, company, workplaceId);
            writeBaseInfo(writerCollection, workplaceInfo, workplaceId);
            writeWomenActivityInfo(writerCollection, workplaceInfo, workplaceId);
            writeCompatibilityOfChildcareAndWork(writerCollection, workplaceInfo, workplaceId);
        }
    }

    private void writeWorkplaceInfoParent(WriterCollection writerCollection, GbizCompany company, Long workplaceId) throws IOException {
        writerCollection.getWorkplaceWriter().write(String.join(",", csv(workplaceId), csv(company.getCorporateNumber())));
        writerCollection.getWorkplaceWriter().newLine();
    }

    private void writeBaseInfo(WriterCollection writerCollection, WorkplaceInfo workplaceInfo, Long workplaceId) throws IOException {
        BaseInfos baseInfos = workplaceInfo.getBaseInfos();
        if (baseInfos != null) {
            writerCollection.getBaseInfoWriter().write(String.join(",", csv(workplaceId), csv(baseInfos.getAverageContinuousServiceYearsType()), csv(baseInfos.getAverageContinuousServiceYearsMale()), csv(baseInfos.getAverageContinuousServiceYearsFemale()), csv(baseInfos.getAverageContinuousServiceYears()), csv(baseInfos.getAverageAge()), csv(baseInfos.getMonthAveragePredeterminedOvertimeHours())));
            writerCollection.getBaseInfoWriter().newLine();
        }
    }

    private void writeWomenActivityInfo(WriterCollection writerCollection, WorkplaceInfo workplaceInfo, Long workplaceId) throws IOException {
        WomenActivityInfos womenActivityInfos = workplaceInfo.getWomenActivityInfos();
        if (womenActivityInfos != null) {
            writerCollection.getWomenActivityWriter().write(String.join(",", csv(workplaceId), csv(womenActivityInfos.getFemaleWorkersProportionType()), csv(womenActivityInfos.getFemaleWorkersProportion()), csv(womenActivityInfos.getFemaleShareOfManager()), csv(womenActivityInfos.getGenderTotalOfManager()), csv(womenActivityInfos.getFemaleShareOfOfficers()), csv(womenActivityInfos.getGenderTotalOfOfficers())));
            writerCollection.getWomenActivityWriter().newLine();
        }
    }

    private void writeCompatibilityOfChildcareAndWork(WriterCollection writerCollection, WorkplaceInfo workplaceInfo, Long workplaceId) throws IOException {
        CompatibilityOfChildcareAndWork compatibilityOfChildcareAndWork = workplaceInfo.getCompatibilityOfChildcareAndWork();
        if (compatibilityOfChildcareAndWork != null) {
            writerCollection.getCompatChildWorkWriter().write(String.join(",", csv(workplaceId), csv(compatibilityOfChildcareAndWork.getNumberOfPaternityLeave()), csv(compatibilityOfChildcareAndWork.getNumberOfMaternityLeave()), csv(compatibilityOfChildcareAndWork.getPaternityLeaveAcquisitionNum()), csv(compatibilityOfChildcareAndWork.getMaternityLeaveAcquisitionNum())));
            writerCollection.getCompatChildWorkWriter().newLine();
        }
    }

    private void writeFinances(WriterCollection writerCollection, GbizCompany company) throws IOException {
        if (company.getFinance() == null) return;
        for (Finance finance : company.getFinance()) {
            Long financeId = financeIdSeq.getAndIncrement();
            writeFinance(writerCollection, finance, financeId, company);
            writeManagementIndex(finance, writerCollection, financeId);
            writeMajorShareholders(finance, writerCollection, financeId);
        }
    }

    private void writeFinance(WriterCollection writerCollection, Finance finance, Long financeId, GbizCompany company) throws IOException {
        writerCollection.getFinanceWriter().write(String.join(",", csv(financeId), csv(company.getCorporateNumber()), csv(finance.getAccountingStandards()), csv(finance.getFiscalYearCoverPage())));
        writerCollection.getFinanceWriter().newLine();
    }

    private void writeManagementIndex(Finance finance, WriterCollection writerCollection, Long financeId) throws IOException {
        if (finance.getManagementIndex() != null) {
            for (ManagementIndex managementIndex : finance.getManagementIndex()) {
                writerCollection.getManagementIndexWriter().write(String.join(",", csv(financeId), csv(managementIndex.getPeriod()), csv(managementIndex.getNetSalesSummaryOfBusinessResults()), csv(managementIndex.getNetSalesSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getOperatingRevenue1SummaryOfBusinessResults()), csv(managementIndex.getOperatingRevenue1SummaryOfBusinessResultsUnitRef()), csv(managementIndex.getOperatingRevenue2SummaryOfBusinessResults()), csv(managementIndex.getOperatingRevenue2SummaryOfBusinessResultsUnitRef()), csv(managementIndex.getGrossOperatingRevenueSummaryOfBusinessResults()), csv(managementIndex.getGrossOperatingRevenueSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getOrdinaryIncomeSummaryOfBusinessResults()), csv(managementIndex.getOrdinaryIncomeSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getNetPremiumsWrittenSummaryOfBusinessResultIns()), csv(managementIndex.getNetPremiumsWrittenSummaryOfBusinessResultsInsUnitRef()), csv(managementIndex.getOrdinaryIncomeLossSummaryOfBusinessResults()), csv(managementIndex.getOrdinaryIncomeLossSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getNetIncomeLossSummaryOfBusinessResults()), csv(managementIndex.getNetIncomeLossSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getCapitalStockSummaryOfBusinessResults()), csv(managementIndex.getCapitalStockSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getNetAssetsSummaryOfBusinessResults()), csv(managementIndex.getNetAssetsSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getTotalAssetsSummaryOfBusinessResults()), csv(managementIndex.getTotalAssetsSummaryOfBusinessResultsUnitRef()), csv(managementIndex.getNumberOfEmployees()), csv(managementIndex.getNumberOfEmployeesUnitRef())));
                writerCollection.getManagementIndexWriter().newLine();
            }
        }
    }

    private void writeMajorShareholders(Finance finance, WriterCollection writerCollection, Long financeId) throws IOException {
        if (finance.getMajorShareholders() != null) {
            for (MajorShareholders majorShareholders : finance.getMajorShareholders()) {
                writerCollection.getMajorShareholderWriter().write(String.join(",", csv(financeId), csv(majorShareholders.getNameMajorShareholders()), csv(majorShareholders.getShareholdingRatio())));
                writerCollection.getMajorShareholderWriter().newLine();
            }
        }
    }

    private void writeStringList(BufferedWriter out, String corpNum, List<String> values) throws IOException {
        if (values == null || values.isEmpty()) return;
        String escapedNum = csv(corpNum);
        for (String val : values) {
            out.write(escapedNum);
            out.write(",");
            out.write(csv(val));
            out.newLine();
        }
    }

    private BufferedWriter createWriter(OutputStream out) {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private String csv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (s.isEmpty()) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}