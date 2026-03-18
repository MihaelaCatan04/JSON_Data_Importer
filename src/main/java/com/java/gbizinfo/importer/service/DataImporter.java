package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.mapper.DataMapper;
import com.java.gbizinfo.importer.mapper.DeleteMapper;
import com.java.gbizinfo.importer.mapper.MergeMapper;
import com.java.gbizinfo.importer.mapper.StagingMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
@Service
public class DataImporter {

    private static final String URL =
            "https://info.gbiz.go.jp/hojin/Download";

    private static final String PARAMS_TEMPLATE =
            "apiToken=%s&downfile=Hojinjoho&meta=META&downenc=UTF-8&isZip=on&downtype=zip";

    private final DataMapper dataMapper;
    private final StagingMapper stagingMapper;
    private final MergeMapper mergeMapper;
    private final DeleteMapper deleteMapper;
    private final TransactionTemplate transactionTemplate;

    @Value("${apiToken}")
    private String apiToken;

    @Value("${local-path}")
    private String localPath;

    public DataImporter(DataMapper dataMapper,
                        StagingMapper stagingMapper,
                        MergeMapper mergeMapper,
                        DeleteMapper deleteMapper,
                        TransactionTemplate transactionTemplate) {
        this.dataMapper = dataMapper;
        this.stagingMapper = stagingMapper;
        this.mergeMapper = mergeMapper;
        this.deleteMapper = deleteMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public void importData() throws Exception {
        String runId = UUID.randomUUID().toString();
        log.info("Starting remote import run {}", runId);

        stagingMapper.clearRunStagingTables();

        boolean success = true;

        HttpURLConnection http = openConnection();
        http.getOutputStream().write(buildParams().getBytes(StandardCharsets.UTF_8));

        try (ZipInputStream zis = new ZipInputStream(http.getInputStream(), StandardCharsets.UTF_8)) {
            success = processZipStream(zis, runId);
        } finally {
            http.disconnect();
        }

        if (success) {
            transactionTemplate.executeWithoutResult(status -> runCleanup());
            log.info("Remote import run {} complete", runId);
        } else {
            log.warn("Remote import run {} finished with failures; cleanup skipped", runId);
        }
    }

    public void importFromLocalFolder() throws Exception {
        Path folder = Path.of(localPath);
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("local-path is not a directory: " + folder);
        }

        String runId = UUID.randomUUID().toString();
        log.info("Starting local import run {} from {}", runId, folder);

        List<Path> files;
        try (var stream = Files.list(folder)) {
            files = stream
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }

        if (files.isEmpty()) {
            log.warn("No .json files found in {}", folder);
            return;
        }

        stagingMapper.clearRunStagingTables();

        log.info("Found {} .json files", files.size());

        boolean success = true;
        for (Path file : files) {
            if (!processLocalJsonFile(file, runId)) {
                success = false;
            }
        }

        if (success) {
            transactionTemplate.executeWithoutResult(status -> runCleanup());
            log.info("Local import run {} complete", runId);
        } else {
            log.warn("Local import run {} finished with failures; cleanup skipped", runId);
        }
    }

    private boolean processZipStream(ZipInputStream zis, String runId) throws IOException {
        boolean success = true;

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.endsWith(".json")) {
                if (!processEntry(zis, name, runId)) {
                    success = false;
                }
            } else {
                log.debug("Skipping non-json zip entry: {}", name);
            }
            zis.closeEntry();
        }

        return success;
    }

    private boolean processLocalJsonFile(Path file, String runId) throws Exception {
        String entryName = file.getFileName().toString();

        try (InputStream is = Files.newInputStream(file);
             ZipInputStream zis = new SingleEntryZipInputStream(is, entryName)) {

            zis.getNextEntry();
            boolean success = processEntry(zis, entryName, runId);
            zis.closeEntry();
            return success;
        }
    }

    private boolean processEntry(ZipInputStream zis, String entryName, String runId) {
        try {
            transactionTemplate.executeWithoutResult(status -> processOrSkip(zis, entryName, runId));
            log.info("Committed: {}", entryName);
            return true;
        } catch (Exception e) {
            log.error("Failed on '{}' — rolled back", entryName, e);
            return false;
        }
    }

    private void processOrSkip(ZipInputStream zis, String entryName, String runId) {
        if (dataMapper.getTimesCheckpointed(runId, entryName) > 0) {
            log.info("Already processed, skipping: {}", entryName);
            return;
        }

        stagingMapper.clearStagingTables();

        CopyContext.set(zis);
        try {
            dataMapper.copy(entryName);
        } finally {
            CopyContext.clear();
        }

        appendRunSnapshot();
        runMerge();
        dataMapper.insertCheckpoint(runId, entryName);
    }

    private HttpURLConnection openConnection() throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection) new URI(URL).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return connection;
    }

    private String buildParams() {
        return String.format(PARAMS_TEMPLATE, apiToken);
    }

    private void runMerge() {
        mergeMapper.mergeCompany();

        mergeMapper.mergeItemInfo();
        mergeMapper.mergeCompanyItem();

        mergeMapper.mergeBaseInfo();
        mergeMapper.mergeWomenActivity();
        mergeMapper.mergeCompatChildcare();

        mergeMapper.truncateCompanyWorkplaceParts();
        mergeMapper.seedCompanyWorkplaceParts();
        mergeMapper.resolveBaseInfoToCompanyParts();
        mergeMapper.resolveWomenActivityToCompanyParts();
        mergeMapper.resolveChildcareToCompanyParts();
        mergeMapper.computeWorkplacePartsMergeKey();
        mergeMapper.mergeWorkplaceInfo();
        mergeMapper.attachWorkplaceToCompany();

        mergeMapper.mergePatent();
        mergeMapper.mergeCompanyPatent();
        mergeMapper.mergeClassification();
        mergeMapper.mergePatentClassification();

        mergeMapper.mergeCertification();
        mergeMapper.mergeCompanyCertification();

        mergeMapper.mergeSubsidy();
        mergeMapper.mergeCompanySubsidy();

        mergeMapper.mergeCommendation();
        mergeMapper.mergeCompanyCommendation();

        mergeMapper.mergeProcurement();
        mergeMapper.mergeCompanyProcurement();

        mergeMapper.mergeFinance();
        mergeMapper.mergeCompanyFinance();
        mergeMapper.mergeMajorShareholder();
        mergeMapper.mergeFinanceShareholder();
        mergeMapper.mergeManagementIndex();
        mergeMapper.mergeFinanceManagement();
    }

    private void runCleanup() {
        deleteMapper.deleteStaleCompanyItems();
        deleteMapper.deleteStaleCompanyPatents();
        deleteMapper.deleteStalePatentClassifications();
        deleteMapper.deleteStaleCertifications();
        deleteMapper.deleteStaleSubsidies();
        deleteMapper.deleteStaleCommendations();
        deleteMapper.deleteStaleProcurements();
        deleteMapper.deleteStaleFinances();
        deleteMapper.deleteStaleFinanceShareholders();
        deleteMapper.deleteStaleFinanceManagement();
    }

    private void appendRunSnapshot() {
        stagingMapper.appendRunCompany();
        stagingMapper.appendRunCompanyItem();
        stagingMapper.appendRunPatent();
        stagingMapper.appendRunClassification();
        stagingMapper.appendRunCertification();
        stagingMapper.appendRunSubsidy();
        stagingMapper.appendRunCommendation();
        stagingMapper.appendRunProcurement();
        stagingMapper.appendRunFinance();
        stagingMapper.appendRunMajorShareholder();
        stagingMapper.appendRunManagementIndex();
    }
}