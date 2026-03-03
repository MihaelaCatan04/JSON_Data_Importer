package com.java.gbizinfo.importer.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.model.GbizCompany;
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
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipInputStream;

@Log4j2
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
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

    private static final String INDUSTRY_COPY =
            "COPY industry (corporate_number, industry_type) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final String BUSINESS_ITEM_COPY =
            "COPY business_item (corporate_number, item_type) FROM STDIN WITH (FORMAT csv, NULL '')";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        if (!COPY_METHOD.equals(ms.getId())) {
            return invocation.proceed();
        }

        PGConnection pgConn = returnPGConnections(invocation);
        ZipInputStream zip = CopyContext.getZip();

        if (zip == null) {
            throw new IllegalStateException("CopyContext ZipInputStream not set before calling copy()");
        }

        executeCopy(pgConn, zip);
        return 1;
    }

    private PGConnection returnPGConnections(Invocation invocation) throws SQLException {
        Executor executor = (Executor) invocation.getTarget();
        Connection conn = executor.getTransaction().getConnection();
        return conn.unwrap(PGConnection.class);
    }

    private void executeCopy(PGConnection pgConn, ZipInputStream zip) throws Exception {
        ByteArrayOutputStream indBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream itemBuf = new ByteArrayOutputStream();

        long count = copyCompany(pgConn, zip, indBuf, itemBuf);
        log.info("company COPY done ({} records). Writing child tables...", count);

        copyIndustries(pgConn, indBuf);
        copyBusinessItems(pgConn, itemBuf);

        log.info("All COPY operations complete.");
    }

    private long copyCompany(PGConnection pgConn, ZipInputStream zip,
                             ByteArrayOutputStream indBuf,
                             ByteArrayOutputStream itemBuf) throws Exception {
        try (
                PGCopyOutputStream compStream = new PGCopyOutputStream(pgConn, COMPANY_COPY);
                BufferedWriter compWriter = createWriter(compStream);
                BufferedWriter indWriter = createWriter(indBuf);
                BufferedWriter itemWriter = createWriter(itemBuf)
        ) {
            long count = processZip(zip, compWriter, indWriter, itemWriter);
            flushWriters(indWriter, itemWriter);
            return count;
        }
    }

    private BufferedWriter createWriter(OutputStream out) {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    private BufferedWriter createWriter(ByteArrayOutputStream buf) {
        return new BufferedWriter(new OutputStreamWriter(buf, StandardCharsets.UTF_8));
    }

    private void flushWriters(BufferedWriter... writers) throws IOException {
        for (BufferedWriter writer : writers) {
            writer.flush();
        }
    }

    private void copyIndustries(PGConnection pgConn, ByteArrayOutputStream indBuf) throws Exception {
        try (PGCopyOutputStream s = new PGCopyOutputStream(pgConn, INDUSTRY_COPY)) {
            s.write(indBuf.toByteArray());
        }
    }

    private void copyBusinessItems(PGConnection pgConn, ByteArrayOutputStream itemBuf) throws Exception {
        try (PGCopyOutputStream s = new PGCopyOutputStream(pgConn, BUSINESS_ITEM_COPY)) {
            s.write(itemBuf.toByteArray());
        }
    }

    private long processZip(ZipInputStream zip,
                            BufferedWriter compOut,
                            BufferedWriter indOut,
                            BufferedWriter itemOut) throws IOException {
        try (MappingIterator<GbizCompany> it = createCompanyIterator(zip)) {
            return processCompanies(it, compOut, indOut, itemOut);
        }
    }

    private MappingIterator<GbizCompany> createCompanyIterator(ZipInputStream zip) throws IOException {
        InputStream nonClosing = new FilterInputStream(zip) {
            @Override
            public void close() {
            }
        };
        return MAPPER.readerFor(GbizCompany.class).readValues(nonClosing);
    }

    private long processCompanies(MappingIterator<GbizCompany> it,
                                  BufferedWriter compOut,
                                  BufferedWriter indOut,
                                  BufferedWriter itemOut) throws IOException {
        long count = 0;
        while (it.hasNext()) {
            GbizCompany company = it.next();
            processSingleCompany(company, compOut, indOut, itemOut);
        }
        return count;
    }

    private void processSingleCompany(GbizCompany company,
                                      BufferedWriter compOut,
                                      BufferedWriter indOut,
                                      BufferedWriter itemOut) throws IOException {
        writeCompanyRow(compOut, company);
        writeChildRows(indOut, company.getCorporateNumber(), company.getIndustry());
        writeChildRows(itemOut, company.getCorporateNumber(), company.getBusinessItems());
    }


    private void writeCompanyRow(BufferedWriter out, GbizCompany c) throws IOException {
        out.write(String.join(",",
                csv(c.getCorporateNumber()),
                csv(c.getName()),
                csv(c.getKana()),
                csv(c.getNameEn()),
                csv(c.getPostalCode()),
                csv(c.getLocation()),
                csv(c.getProcess()),
                csv(c.getAggregatedYear()),
                csv(c.getStatus()),
                csv(c.getCloseDate()),
                csv(c.getCloseCause()),
                csv(c.getKind()),
                csv(c.getRepresentativeName()),
                csv(c.getCapitalStock()),
                csv(c.getEmployeeNumber()),
                csv(c.getCompanySizeMale()),
                csv(c.getCompanySizeFemale()),
                csv(c.getBusinessSummary()),
                csv(c.getCompanyUrl()),
                csv(c.getFoundingYear()),
                csv(c.getDateOfEstablishment()),
                csv(c.getQualificationGrade()),
                csv(c.getUpdateDate())
        ));
        out.newLine();
    }

    private void writeChildRows(BufferedWriter out, String corpNum, List<String> values) throws IOException {
        if (values == null || values.isEmpty()) return;
        String escapedNum = csv(corpNum);
        for (String val : values) {
            out.write(escapedNum);
            out.write(",");
            out.write(csv(val));
            out.newLine();
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