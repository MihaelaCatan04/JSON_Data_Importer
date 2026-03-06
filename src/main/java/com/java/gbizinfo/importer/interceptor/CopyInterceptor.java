package com.java.gbizinfo.importer.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.model.company.*;
import com.java.gbizinfo.importer.model.cursor.IdCursor;
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
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.zip.ZipInputStream;

@Log4j2
@Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class CopyInterceptor implements Interceptor {

    private static final String COPY_METHOD = "com.java.gbizinfo.importer.mapper.DataMapper.copy";

    private static final String COMPANY_COPY = """
            COPY company (
                company_id,
                corporate_number, name, kana, name_en, postal_code, location,
                process, aggregated_year, status, close_date, close_status, kind,
                representative_name, capital_stock, employee_number,
                company_size_male, company_size_female, business_summary,
                company_url, founding_year, date_of_establishment,
                qualification_grade, update_date, updated_at
            ) FROM STDIN WITH (FORMAT csv, NULL '')
            """;

    private static final String ITEM_INFO_COPY = "COPY item_info (info_id, value, is_industry) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String COMPANY_ITEM_COPY = "COPY company_item (company_id, info_id) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String SELECT_ITEM_INFO = "SELECT info_id, value, is_industry FROM item_info";

    private static final String PATENT_COPY = "COPY patent (patent_id, company_id, patent_type, registration_number, application_date, title, url) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String CLASSIFICATION_COPY = "COPY classification (patent_id, code_value, code_name, japanese) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String CERTIFICATION_COPY = "COPY certification (company_id, date_of_approval, title, target, government_departments, category) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String SUBSIDY_COPY = "COPY subsidy (company_id, date_of_approval, title, amount, target, government_departments) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String WORKPLACE_INFO_COPY = "COPY workplace_info (workplace_info_id, company_id) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String BASE_INFO_COPY = "COPY base_info (workplace_info_id, average_continuous_service_years_type, average_continuous_service_years_male, average_continuous_service_years_female, average_continuous_service_years, average_age, month_average_predetermined_overtime_hours) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String WOMEN_ACTIVITY_INFO_COPY = "COPY women_activity_info (workplace_info_id, female_workers_proportion_type, female_workers_proportion, female_share_of_manager, gender_total_of_manager, female_share_of_officers, gender_total_of_officers) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String COMPAT_CHILDCARE_COPY = "COPY compatibility_of_childcare_and_work (workplace_info_id, number_of_paternity_leave, number_of_maternity_leave, paternity_leave_acquisition_num, maternity_leave_acquisition_num) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String COMMENDATION_COPY = "COPY commendation (company_id, date_of_commendation, title, target, category, government_departments, note) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String PROCUREMENT_COPY = "COPY procurement (company_id, date_of_order, title, amount, government_departments, note) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String FINANCE_COPY = "COPY finance (finance_id, company_id, accounting_standards, fiscal_year_cover_page) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String MAJOR_SHAREHOLDER_COPY = "COPY major_shareholder (finance_id, name_major_stakeholders, shareholding_ratio) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String MANAGEMENT_INDEX_COPY = "COPY management_index (finance_id, period, net_sales_summary_of_business_results, net_sales_summary_of_business_results_unit_ref, operating_revenue1_summary_of_business_results, operating_revenue1_summary_of_business_results_unit_ref, operating_revenue2_summary_of_business_results, operating_revenue2_summary_of_business_results_unit_ref, gross_operating_revenue_summary_of_business_results, gross_operating_revenue_summary_of_business_results_unit_ref, ordinary_income_summary_of_business_results, ordinary_income_summary_of_business_results_unit_ref, net_premiums_written_summary_of_business_results_ins, net_premiums_written_summary_of_business_results_ins_unit_ref, ordinary_income_loss_summary_of_business_results, ordinary_income_loss_summary_of_business_results_unit_ref, net_income_loss_summary_of_business_results, net_income_loss_summary_of_business_results_unit_ref, capital_stock_summary_of_business_results, capital_stock_summary_of_business_results_unit_ref, net_assets_summary_of_business_results, net_assets_summary_of_business_results_unit_ref, total_assets_summary_of_business_results, total_assets_summary_of_business_results_unit_ref, number_of_employees, number_of_employees_unit_ref) FROM STDIN WITH (FORMAT csv, NULL '')";
    private static final String SELECT_BY_CORPORATE_NUMBER = "SELECT company_id, corporate_number, updated_at FROM company WHERE corporate_number = ANY(?)";
    private static final String UPDATE_COMPANY = """
            UPDATE company SET
                name = ?, kana = ?, name_en = ?, postal_code = ?, location = ?,
                process = ?, aggregated_year = ?, status = ?, close_date = ?,
                close_status = ?, kind = ?, representative_name = ?, capital_stock = ?,
                employee_number = ?, company_size_male = ?, company_size_female = ?,
                business_summary = ?, company_url = ?, founding_year = ?,
                date_of_establishment = ?, qualification_grade = ?,
                update_date = ?, updated_at = ?
            WHERE company_id = ?
            """;

    private static final List<String> CHILD_TABLES = List.of("company_item", "patent", "certification", "subsidy", "commendation", "procurement", "workplace_info", "finance");

    private static final String DELETE_SQL_TEMPLATE = "DELETE FROM %s WHERE company_id = ANY(?)";
    private static final String SELECT_VAL = "SELECT nextval(?) FROM generate_series(1, ?)";

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    private static final String COMPANY_ID_SEQ = "company_id_seq";
    private static final String PATENT_ID_SEQ = "patent_id_seq";
    private static final String WORKPLACE_INFO_ID_SEQ = "workplace_info_id_seq";
    private static final String FINANCE_ID_SEQ = "finance_id_seq";
    private static final String ITEM_INFO_ID_SEQ = "item_info_id_seq";

    private static IdCursor companyIdCursor;
    private static IdCursor patentIdCursor;
    private static IdCursor workplaceIdCursor;
    private static IdCursor financeIdCursor;

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

        executeCopy(pgConn, conn, zip);
        return 1;
    }

    private void executeCopy(PGConnection pgConn, Connection conn, ZipInputStream zip) throws Exception {
        List<GbizCompany> companies = readCompanies(zip);
        Map<String, ExistingCompany> existing = findExistingCompanies(conn, companies);

        CompanyProcessingResult result = classify(companies, existing);

        List<GbizCompany> combinedData = combineNewAndChanged(result);

        Map<String, Long> infoCache = loadInfoCache(conn);
        registerNewInfoValues(conn, combinedData, infoCache);

        initializeIdCursors(conn, result, combinedData);

        OutputStreamCollection streams = new OutputStreamCollection();
        WriterCollection writers = new WriterCollection(streams);

        processCompanies(pgConn, conn, result, writers, infoCache);

        finalizeCopy(pgConn, conn, result, writers, streams);
    }

    private String infoKey(boolean isIndustry, String value) {
        return isIndustry + ":" + value;
    }

    private Map<String, Long> loadInfoCache(Connection conn) throws SQLException {
        Map<String, Long> cache = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ITEM_INFO); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long infoId = rs.getLong("info_id");
                String value = rs.getString("value");
                boolean isIndustry = rs.getBoolean("is_industry");
                cache.put(infoKey(isIndustry, value), infoId);
            }
        }
        return cache;
    }

    private void registerNewInfoValues(Connection conn,
                                       List<GbizCompany> companies,
                                       Map<String, Long> infoCache) throws Exception {
        LinkedHashMap<String, Boolean> newEntries = collectNewEntries(companies, infoCache);

        if (newEntries.isEmpty()) {
            log.info("No new item_info entries needed.");
            return;
        }

        IdCursor infoIdCursor = fetchIds(conn, ITEM_INFO_ID_SEQ, newEntries.size());
        writeEntries(conn, newEntries, infoIdCursor, infoCache);
    }

    private LinkedHashMap<String, Boolean> collectNewEntries(List<GbizCompany> companies,
                                                             Map<String, Long> infoCache) {
        LinkedHashMap<String, Boolean> newEntries = new LinkedHashMap<>();
        for (GbizCompany c : companies) {
            collectNewInfo(c.getIndustry(), true, infoCache, newEntries);
            collectNewInfo(c.getBusinessItems(), false, infoCache, newEntries);
        }
        return newEntries;
    }

    private void writeEntries(Connection conn,
                              LinkedHashMap<String, Boolean> newEntries,
                              IdCursor infoIdCursor,
                              Map<String, Long> infoCache) throws Exception {
        PGConnection pgConn = conn.unwrap(PGConnection.class);
        try (PGCopyOutputStream out = new PGCopyOutputStream(pgConn, ITEM_INFO_COPY);
             BufferedWriter w = createWriter(out)) {

            for (Map.Entry<String, Boolean> entry : newEntries.entrySet()) {
                long infoId = infoIdCursor.moveNext();
                boolean isIndustry = entry.getValue();
                String value = extractValue(entry.getKey());

                w.write(String.join(",", csv(infoId), csv(value), csv(isIndustry)));
                w.newLine();

                infoCache.put(entry.getKey(), infoId);
            }
        }
    }

    private String extractValue(String rawKey) {
        return rawKey.substring(rawKey.indexOf(':') + 1);
    }


    private void collectNewInfo(List<String> values, boolean isIndustry, Map<String, Long> cache, LinkedHashMap<String, Boolean> newEntries) {
        if (values == null) return;
        for (String v : values) {
            if (v == null || v.isBlank()) continue;
            String key = infoKey(isIndustry, v);
            if (cache.containsKey(key) && newEntries.containsKey(key)) continue;
            newEntries.put(key, isIndustry);
        }
    }

    private CompanyProcessingResult classify(List<GbizCompany> companies, Map<String, ExistingCompany> existing) {
        CompanyProcessingResult result = classifyCompanies(companies, existing);
        logClassification(result);
        return result;
    }

    private List<GbizCompany> combineNewAndChanged(CompanyProcessingResult result) {
        List<GbizCompany> combinedData = new ArrayList<>(result.getNewCompanies());
        result.getChangedCompanies().forEach(changedCompany -> combinedData.add(changedCompany.getData()));
        return combinedData;
    }

    private void initializeIdCursors(Connection conn, CompanyProcessingResult result, List<GbizCompany> combinedData) throws SQLException {
        companyIdCursor = fetchIds(conn, COMPANY_ID_SEQ, result.getNewCompanies().size());
        patentIdCursor = fetchIds(conn, PATENT_ID_SEQ, countPatents(combinedData));
        workplaceIdCursor = fetchIds(conn, WORKPLACE_INFO_ID_SEQ, countWorkplaces(combinedData));
        financeIdCursor = fetchIds(conn, FINANCE_ID_SEQ, countFinances(combinedData));
    }

    private void processCompanies(PGConnection pgConn, Connection conn, CompanyProcessingResult result, WriterCollection writers, Map<String, Long> infoCache) throws Exception {
        processNewCompanies(pgConn, result.getNewCompanies(), writers, infoCache);
        processChangedCompanies(conn, result.getChangedCompanies(), writers, infoCache);
    }

    private void finalizeCopy(PGConnection pgConn, Connection conn, CompanyProcessingResult result, WriterCollection writers, OutputStreamCollection streams) throws Exception {
        writers.flushAll();
        cleanupStaleChildren(conn, result.getChangedCompanies());
        copyBufferedStreams(pgConn, streams);
        log.info("All COPY operations complete.");
    }

    private IdCursor fetchIds(Connection conn, String sequenceName, int count) throws SQLException {
        if (count == 0) return new IdCursor(List.of());

        try (PreparedStatement ps = conn.prepareStatement(SELECT_VAL)) {
            ps.setString(1, sequenceName);
            ps.setInt(2, count);

            ResultSet rs = ps.executeQuery();
            List<Long> ids = new ArrayList<>(count);
            while (rs.next()) ids.add(rs.getLong(1));
            return new IdCursor(ids);
        }
    }

    private int countPatents(List<GbizCompany> companies) {
        return companies.stream().mapToInt(c -> c.getPatent() == null ? 0 : c.getPatent().size()).sum();
    }

    private int countWorkplaces(List<GbizCompany> companies) {
        return companies.stream().mapToInt(c -> c.getWorkplaceInfo() == null ? 0 : c.getWorkplaceInfo().size()).sum();
    }

    private int countFinances(List<GbizCompany> companies) {
        return companies.stream().mapToInt(c -> c.getFinance() == null ? 0 : c.getFinance().size()).sum();
    }

    private List<GbizCompany> readCompanies(ZipInputStream zip) throws Exception {
        List<GbizCompany> companies = readAllCompanies(zip);
        log.info("Read {} companies from ZIP entry.", companies.size());
        return companies;
    }

    private Map<String, ExistingCompany> findExistingCompanies(Connection conn, List<GbizCompany> companies) throws SQLException {
        Map<String, ExistingCompany> existing = loadExistingCompanies(conn, companies);
        log.info("Found {} already in DB.", existing.size());
        return existing;
    }

    private CompanyProcessingResult classifyCompanies(List<GbizCompany> companies, Map<String, ExistingCompany> existing) {
        List<GbizCompany> newCompanies = new ArrayList<>();
        List<ChangedCompany> changedCompanies = new ArrayList<>();
        int skipped = 0;

        for (GbizCompany company : companies) {
            OffsetDateTime maxLastUpdate = resolveMaxLastUpdateDate(company);
            ExistingCompany ex = existing.get(company.getCorporateNumber());

            if (ex == null) {
                newCompanies.add(company);
            } else if (isNewer(maxLastUpdate, ex.getUpdatedAt())) {
                changedCompanies.add(new ChangedCompany(ex.getCompanyId(), company, maxLastUpdate));
            } else {
                skipped++;
            }
        }
        return new CompanyProcessingResult(newCompanies, changedCompanies, skipped);
    }

    private void logClassification(CompanyProcessingResult result) {
        log.info("New: {}, changed: {}, unchanged (skipped): {}", result.getNewCompanies().size(), result.getChangedCompanies().size(), result.getSkipped());
    }

    private void processNewCompanies(PGConnection pgConn, List<GbizCompany> newCompanies, WriterCollection writers, Map<String, Long> infoCache) throws Exception {
        if (newCompanies.isEmpty()) return;

        try (PGCopyOutputStream compStream = new PGCopyOutputStream(pgConn, COMPANY_COPY); BufferedWriter compWriter = createWriter(compStream)) {

            for (GbizCompany company : newCompanies) {
                long companyId = companyIdCursor.moveNext();
                OffsetDateTime updatedAt = resolveMaxLastUpdateDate(company);
                writeCompanyRow(compWriter, company, companyId, updatedAt);
                writeAllChildren(writers, company, companyId, infoCache);
            }
        }
        log.info("Inserted {} new companies via COPY.", newCompanies.size());
    }

    private void processChangedCompanies(Connection conn, List<ChangedCompany> changedCompanies, WriterCollection writers, Map<String, Long> infoCache) throws SQLException, IOException {
        for (ChangedCompany cc : changedCompanies) {
            updateCompanyRow(conn, cc);
            writeAllChildren(writers, cc.getData(), cc.getCompanyId(), infoCache);
        }
    }

    private void cleanupStaleChildren(Connection conn, List<ChangedCompany> changedCompanies) throws SQLException {
        if (!changedCompanies.isEmpty()) {
            deleteStaleChildren(conn, changedCompanies.stream().map(ChangedCompany::getCompanyId).toList());
        }
    }

    private List<GbizCompany> readAllCompanies(ZipInputStream zip) throws IOException {
        InputStream nonClosing = new FilterInputStream(zip) {
            @Override
            public void close() { /* intentionally empty */ }
        };
        List<GbizCompany> result = new ArrayList<>();
        try (MappingIterator<GbizCompany> it = MAPPER.readerFor(GbizCompany.class).readValues(nonClosing)) {
            while (it.hasNext()) result.add(it.next());
        }
        return result;
    }

    private Map<String, ExistingCompany> loadExistingCompanies(Connection conn, List<GbizCompany> companies) throws SQLException {
        if (companies.isEmpty()) return Collections.emptyMap();

        List<String> corpNums = extractCorporateNumbers(companies);
        Array sqlArray = createSqlArray(conn, corpNums);

        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_CORPORATE_NUMBER)) {
            ps.setArray(1, sqlArray);
            ResultSet rs = ps.executeQuery();
            return buildCompanyMap(rs);
        }
    }

    private List<String> extractCorporateNumbers(List<GbizCompany> companies) {
        return companies.stream().map(GbizCompany::getCorporateNumber).filter(Objects::nonNull).toList();
    }

    private Array createSqlArray(Connection conn, List<String> corpNums) throws SQLException {
        return conn.createArrayOf("VARCHAR", corpNums.toArray(String[]::new));
    }

    private Map<String, ExistingCompany> buildCompanyMap(ResultSet rs) throws SQLException {
        Map<String, ExistingCompany> map = new HashMap<>();
        while (rs.next()) {
            map.put(rs.getString("corporate_number"), new ExistingCompany(rs.getLong("company_id"), rs.getObject("updated_at", OffsetDateTime.class)));
        }
        return map;
    }

    private void updateCompanyRow(Connection conn, ChangedCompany cc) throws SQLException {
        GbizCompany c = cc.getData();
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_COMPANY)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getKana());
            ps.setString(3, c.getNameEn());
            ps.setString(4, c.getPostalCode());
            ps.setString(5, c.getLocation());
            ps.setString(6, c.getProcess());
            ps.setString(7, c.getAggregatedYear());
            ps.setString(8, c.getStatus());
            ps.setObject(9, parseLocalDate(c.getCloseDate()));
            ps.setString(10, c.getCloseCause());
            ps.setString(11, c.getKind());
            ps.setString(12, c.getRepresentativeName());
            ps.setObject(13, c.getCapitalStock());
            ps.setObject(14, c.getEmployeeNumber());
            ps.setObject(15, c.getCompanySizeMale());
            ps.setObject(16, c.getCompanySizeFemale());
            ps.setString(17, c.getBusinessSummary());
            ps.setString(18, c.getCompanyUrl());
            ps.setObject(19, c.getFoundingYear());
            ps.setObject(20, parseLocalDate(c.getDateOfEstablishment()));
            ps.setString(21, c.getQualificationGrade());
            ps.setObject(22, parseOffsetDateTime(c.getUpdateDate()));
            ps.setObject(23, cc.getNewUpdatedAt());
            ps.setLong(24, cc.getCompanyId());
            int rows = ps.executeUpdate();
            if (rows == 0) log.warn("UPDATE matched 0 rows for company_id={}", cc.getCompanyId());
        }
    }

    private void deleteStaleChildren(Connection conn, List<Long> companyIds) throws SQLException {
        Array sqlArray = conn.createArrayOf("BIGINT", companyIds.toArray(Long[]::new));
        for (String table : CHILD_TABLES) {
            String sql = String.format(DELETE_SQL_TEMPLATE, table);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                executeDelete(ps, sqlArray, table, companyIds);
            }
        }
    }

    private void executeDelete(PreparedStatement ps, Array sqlArray, String table, List<Long> companyIds) throws SQLException {
        ps.setArray(1, sqlArray);
        int deleted = ps.executeUpdate();
        if (deleted > 0) {
            log.debug("Deleted {} stale rows from {} for {} changed companies.", deleted, table, companyIds.size());
        }
    }


    private void writeAllChildren(WriterCollection writers, GbizCompany company, long companyId, Map<String, Long> infoCache) throws IOException {
        writeCompanyItems(writers.getCompanyItemWriter(), companyId, company.getIndustry(), true, infoCache);
        writeCompanyItems(writers.getCompanyItemWriter(), companyId, company.getBusinessItems(), false, infoCache);
        writePatents(writers, company, companyId);
        writeCertifications(writers, company, companyId);
        writeSubsidies(writers, company, companyId);
        writeCommendations(writers, company, companyId);
        writeProcurements(writers, company, companyId);
        writeWorkplaceInfos(writers, company, companyId);
        writeFinances(writers, company, companyId);
    }

    private void writeCompanyItems(BufferedWriter out, long companyId, List<String> values, boolean isIndustry, Map<String, Long> infoCache) throws IOException {
        if (values == null || values.isEmpty()) return;
        String escapedId = csv(companyId);
        for (String val : values) {
            if (val == null || val.isBlank()) continue;
            Long infoId = infoCache.get(infoKey(isIndustry, val));
            if (infoId == null) {
                log.warn("Meta ID not found for isIndustry={} value='{}' — skipping row.", isIndustry, val);
                continue;
            }
            out.write(escapedId);
            out.write(",");
            out.write(csv(infoId));
            out.newLine();
        }
    }

    private void copyBufferedStreams(PGConnection pgConn, OutputStreamCollection s) throws Exception {
        writeBuffer(pgConn, COMPANY_ITEM_COPY, s.getCompanyItemBuf());
        writeBuffer(pgConn, PATENT_COPY, s.getPatentBuf());
        writeBuffer(pgConn, CLASSIFICATION_COPY, s.getClassificationBuf());
        writeBuffer(pgConn, CERTIFICATION_COPY, s.getCertificationBuf());
        writeBuffer(pgConn, SUBSIDY_COPY, s.getSubsidyBuf());
        writeBuffer(pgConn, COMMENDATION_COPY, s.getCommendationBuf());
        writeBuffer(pgConn, PROCUREMENT_COPY, s.getProcurementBuf());
        writeBuffer(pgConn, WORKPLACE_INFO_COPY, s.getWorkplaceBuf());
        writeBuffer(pgConn, BASE_INFO_COPY, s.getBaseInfoBuf());
        writeBuffer(pgConn, WOMEN_ACTIVITY_INFO_COPY, s.getWomenActivityBuf());
        writeBuffer(pgConn, COMPAT_CHILDCARE_COPY, s.getCompatChildWorkBuf());
        writeBuffer(pgConn, FINANCE_COPY, s.getFinanceBuf());
        writeBuffer(pgConn, MAJOR_SHAREHOLDER_COPY, s.getMajorShareholderBuf());
        writeBuffer(pgConn, MANAGEMENT_INDEX_COPY, s.getManagementIndexBuf());
    }

    private void writeBuffer(PGConnection pgConn, String sql, ByteArrayOutputStream buf) throws Exception {
        byte[] data = buf.toByteArray();
        if (data.length == 0) return;
        try (PGCopyOutputStream out = new PGCopyOutputStream(pgConn, sql)) {
            out.write(data);
        }
    }

    private void writeCompanyRow(BufferedWriter out, GbizCompany c, long companyId, OffsetDateTime updatedAt) throws IOException {
        out.write(String.join(",", csv(companyId), csv(c.getCorporateNumber()), csv(c.getName()), csv(c.getKana()), csv(c.getNameEn()), csv(c.getPostalCode()), csv(c.getLocation()), csv(c.getProcess()), csv(c.getAggregatedYear()), csv(c.getStatus()), csv(c.getCloseDate()), csv(c.getCloseCause()), csv(c.getKind()), csv(c.getRepresentativeName()), csv(c.getCapitalStock()), csv(c.getEmployeeNumber()), csv(c.getCompanySizeMale()), csv(c.getCompanySizeFemale()), csv(c.getBusinessSummary()), csv(c.getCompanyUrl()), csv(c.getFoundingYear()), csv(c.getDateOfEstablishment()), csv(c.getQualificationGrade()), csv(c.getUpdateDate()), csv(updatedAt)));
        out.newLine();
    }

    private void writePatents(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getPatent() == null) return;
        for (Patent patent : company.getPatent()) {
            long patentId = patentIdCursor.moveNext();
            w.getPatentWriter().write(String.join(",", csv(patentId), csv(companyId), csv(patent.getPatentType()), csv(patent.getRegistrationNumber()), csv(patent.getApplicationDate()), csv(patent.getTitle()), csv(patent.getUrl())));
            w.getPatentWriter().newLine();
            writeClassifications(w, patentId, patent.getClassifications());
        }
    }

    private void writeClassifications(WriterCollection w, long patentId, List<Classifications> list) throws IOException {
        if (list == null) return;
        for (Classifications cl : list) {
            w.getClassificationWriter().write(String.join(",", csv(patentId), csv(cl.getCodeValue()), csv(cl.getCodeName()), csv(cl.getJapanese())));
            w.getClassificationWriter().newLine();
        }
    }

    private void writeCertifications(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getCertification() == null) return;
        for (Certification cert : company.getCertification()) {
            w.getCertificationWriter().write(String.join(",", csv(companyId), csv(cert.getDateOfApproval()), csv(cert.getTitle()), csv(cert.getTarget()), csv(cert.getGovernmentDepartments()), csv(cert.getCategory())));
            w.getCertificationWriter().newLine();
        }
    }

    private void writeSubsidies(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getSubsidy() == null) return;
        for (Subsidy sub : company.getSubsidy()) {
            w.getSubsidyWriter().write(String.join(",", csv(companyId), csv(sub.getDateOfApproval()), csv(sub.getTitle()), csv(sub.getAmount()), csv(sub.getTarget()), csv(sub.getGovernmentDepartments())));
            w.getSubsidyWriter().newLine();
        }
    }

    private void writeCommendations(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getCommendation() == null) return;
        for (Commendation com : company.getCommendation()) {
            w.getCommendationWriter().write(String.join(",", csv(companyId), csv(com.getDateOfCommendation()), csv(com.getTitle()), csv(com.getTarget()), csv(com.getCategory()), csv(com.getGovernmentDepartments()), csv(com.getNote())));
            w.getCommendationWriter().newLine();
        }
    }

    private void writeProcurements(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getProcurement() == null) return;
        for (Procurement proc : company.getProcurement()) {
            w.getProcurementWriter().write(String.join(",", csv(companyId), csv(proc.getDateOfOrder()), csv(proc.getTitle()), csv(proc.getAmount()), csv(proc.getGovernmentDepartments()), csv(proc.getNote())));
            w.getProcurementWriter().newLine();
        }
    }

    private void writeWorkplaceInfos(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getWorkplaceInfo() == null) return;
        for (WorkplaceInfo wi : company.getWorkplaceInfo()) {
            long workplaceId = workplaceIdCursor.moveNext();
            w.getWorkplaceWriter().write(String.join(",", csv(workplaceId), csv(companyId)));
            w.getWorkplaceWriter().newLine();
            writeBaseInfo(w, wi, workplaceId);
            writeWomenActivityInfo(w, wi, workplaceId);
            writeCompatibilityOfChildcareAndWork(w, wi, workplaceId);
        }
    }

    private void writeBaseInfo(WriterCollection w, WorkplaceInfo wi, long workplaceId) throws IOException {
        BaseInfos b = wi.getBaseInfos();
        if (b == null) return;
        w.getBaseInfoWriter().write(String.join(",", csv(workplaceId), csv(b.getAverageContinuousServiceYearsType()), csv(b.getAverageContinuousServiceYearsMale()), csv(b.getAverageContinuousServiceYearsFemale()), csv(b.getAverageContinuousServiceYears()), csv(b.getAverageAge()), csv(b.getMonthAveragePredeterminedOvertimeHours())));
        w.getBaseInfoWriter().newLine();
    }

    private void writeWomenActivityInfo(WriterCollection w, WorkplaceInfo wi, long workplaceId) throws IOException {
        WomenActivityInfos wa = wi.getWomenActivityInfos();
        if (wa == null) return;
        w.getWomenActivityWriter().write(String.join(",", csv(workplaceId), csv(wa.getFemaleWorkersProportionType()), csv(wa.getFemaleWorkersProportion()), csv(wa.getFemaleShareOfManager()), csv(wa.getGenderTotalOfManager()), csv(wa.getFemaleShareOfOfficers()), csv(wa.getGenderTotalOfOfficers())));
        w.getWomenActivityWriter().newLine();
    }

    private void writeCompatibilityOfChildcareAndWork(WriterCollection w, WorkplaceInfo wi, long workplaceId) throws IOException {
        CompatibilityOfChildcareAndWork cc = wi.getCompatibilityOfChildcareAndWork();
        if (cc == null) return;
        w.getCompatChildWorkWriter().write(String.join(",", csv(workplaceId), csv(cc.getNumberOfPaternityLeave()), csv(cc.getNumberOfMaternityLeave()), csv(cc.getPaternityLeaveAcquisitionNum()), csv(cc.getMaternityLeaveAcquisitionNum())));
        w.getCompatChildWorkWriter().newLine();
    }

    private void writeFinances(WriterCollection w, GbizCompany company, long companyId) throws IOException {
        if (company.getFinance() == null) return;
        for (Finance finance : company.getFinance()) {
            long financeId = financeIdCursor.moveNext();
            w.getFinanceWriter().write(String.join(",", csv(financeId), csv(companyId), csv(finance.getAccountingStandards()), csv(finance.getFiscalYearCoverPage())));
            w.getFinanceWriter().newLine();
            writeManagementIndex(finance, w, financeId);
            writeMajorShareholders(finance, w, financeId);
        }
    }

    private void writeManagementIndex(Finance finance, WriterCollection w, long financeId) throws IOException {
        if (finance.getManagementIndex() == null) return;
        for (ManagementIndex mi : finance.getManagementIndex()) {
            w.getManagementIndexWriter().write(String.join(",", csv(financeId), csv(mi.getPeriod()), csv(mi.getNetSalesSummaryOfBusinessResults()), csv(mi.getNetSalesSummaryOfBusinessResultsUnitRef()), csv(mi.getOperatingRevenue1SummaryOfBusinessResults()), csv(mi.getOperatingRevenue1SummaryOfBusinessResultsUnitRef()), csv(mi.getOperatingRevenue2SummaryOfBusinessResults()), csv(mi.getOperatingRevenue2SummaryOfBusinessResultsUnitRef()), csv(mi.getGrossOperatingRevenueSummaryOfBusinessResults()), csv(mi.getGrossOperatingRevenueSummaryOfBusinessResultsUnitRef()), csv(mi.getOrdinaryIncomeSummaryOfBusinessResults()), csv(mi.getOrdinaryIncomeSummaryOfBusinessResultsUnitRef()), csv(mi.getNetPremiumsWrittenSummaryOfBusinessResultIns()), csv(mi.getNetPremiumsWrittenSummaryOfBusinessResultsInsUnitRef()), csv(mi.getOrdinaryIncomeLossSummaryOfBusinessResults()), csv(mi.getOrdinaryIncomeLossSummaryOfBusinessResultsUnitRef()), csv(mi.getNetIncomeLossSummaryOfBusinessResults()), csv(mi.getNetIncomeLossSummaryOfBusinessResultsUnitRef()), csv(mi.getCapitalStockSummaryOfBusinessResults()), csv(mi.getCapitalStockSummaryOfBusinessResultsUnitRef()), csv(mi.getNetAssetsSummaryOfBusinessResults()), csv(mi.getNetAssetsSummaryOfBusinessResultsUnitRef()), csv(mi.getTotalAssetsSummaryOfBusinessResults()), csv(mi.getTotalAssetsSummaryOfBusinessResultsUnitRef()), csv(mi.getNumberOfEmployees()), csv(mi.getNumberOfEmployeesUnitRef())));
            w.getManagementIndexWriter().newLine();
        }
    }

    private void writeMajorShareholders(Finance finance, WriterCollection w, long financeId) throws IOException {
        if (finance.getMajorShareholders() == null) return;
        for (MajorShareholders ms : finance.getMajorShareholders()) {
            w.getMajorShareholderWriter().write(String.join(",", csv(financeId), csv(ms.getNameMajorShareholders()), csv(ms.getShareholdingRatio())));
            w.getMajorShareholderWriter().newLine();
        }
    }

    private OffsetDateTime resolveMaxLastUpdateDate(GbizCompany company) {
        OffsetDateTime max = resolveFromMetaData(company.getMetaData());
        if (max == null) max = parseDate(company.getUpdateDate());
        return max;
    }

    private OffsetDateTime resolveFromMetaData(MetaData meta) {
        if (meta == null || meta.getLastUpdateDate() == null) return null;
        OffsetDateTime max = null;
        for (String raw : meta.getLastUpdateDate().values()) {
            OffsetDateTime dt = parseDate(raw);
            if (dt != null && (max == null || dt.isAfter(max))) max = dt;
        }
        return max;
    }

    private OffsetDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return OffsetDateTime.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isNewer(OffsetDateTime candidate, OffsetDateTime stored) {
        if (candidate == null) return false;
        if (stored == null) return true;
        return candidate.isAfter(stored);
    }

    private BufferedWriter createWriter(OutputStream out) {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private java.sql.Date parseLocalDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return java.sql.Date.valueOf(raw.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private OffsetDateTime parseOffsetDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return OffsetDateTime.parse(raw);
        } catch (Exception e) {
            return null;
        }
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