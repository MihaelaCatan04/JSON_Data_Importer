package com.java.gbizinfo.importer.interceptor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.gbizinfo.importer.buffer.StagingBuffer;
import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.model.company.GbizCompany;
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
import java.io.InputStream;
import java.sql.Connection;
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
        streamIntoBuffer(zip);

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

    private void streamIntoBuffer(ZipInputStream zip) throws Exception {
        InputStream nonClosing = new FilterInputStream(zip) {
            @Override
            public void close() {
                // zip entry is managed outside
            }
        };

        try (MappingIterator<GbizCompany> iterator = MAPPER.readerFor(GbizCompany.class).readValues(nonClosing)) {
            while (iterator.hasNextValue()) {
                GbizCompany company = iterator.nextValue();
                company.writeRow();
                company.writeIndustry();
                company.writeBusiness();
                company.writePatent();
                company.writeCertification();
                company.writeSubsidies();
                company.writeCommendations();
                company.writeProcurements();
                company.writeWorkplaceInfo();
                company.writeFinances();
            }
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