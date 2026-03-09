package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.mapper.DataMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
public class DataImporter {

    private static final String URL = "https://info.gbiz.go.jp/hojin/Download";
    private static final String PARAMS_TEMPLATE = "apiToken=%s&downfile=Hojinjoho&meta=META&downenc=UTF-8&isZip=on&downtype=zip";

    @Value("${apiToken}")
    private String apiToken;

    @Value("${local-path}")
    private String localPath;

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void importData() throws Exception {
        String runId = startRun();

        HttpURLConnection http = openConnection();
        http.getOutputStream().write(buildParams().getBytes(StandardCharsets.UTF_8));

        try (ZipInputStream zis = new ZipInputStream(http.getInputStream())) {
            processZipStream(zis, runId);
        } finally {
            http.disconnect();
        }

        finishRun(runId);
    }

    private void processZipStream(ZipInputStream zis, String runId) throws IOException {
        ZipEntry entry;

        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();

            if (isJsonFile(entryName)) {
                processZipEntry(zis, entryName, runId);
            } else {
                log.debug("Skipping non-JSON entry: {}", entryName);
            }

            zis.closeEntry();
        }
    }

    public void importFromLocalFolder() throws Exception {
        Path folder = validateFolder(localPath);
        String runId = startRun();

        List<Path> jsonFiles = listJsonFiles(folder);

        if (jsonFiles.isEmpty()) {
            log.warn("No .json files found in {}", folder);
            return;
        }

        log.info("Found {} .json files to process.", jsonFiles.size());

        for (Path jsonFile : jsonFiles) {
            processLocalFile(jsonFile, runId);
        }

        finishRun(runId);
    }

    private void processLocalFile(Path jsonFile, String runId) throws Exception {
        String entryName = jsonFile.getFileName().toString();

        try (InputStream is = Files.newInputStream(jsonFile); ZipInputStream zis = wrapAsZipInputStream(is, entryName)) {

            zis.getNextEntry();
            processZipEntry(zis, entryName, runId);
        }
    }

    private void processZipEntry(ZipInputStream zis, String entryName, String runId) {
        try {
            transactionTemplate.executeWithoutResult(status -> processOrSkip(zis, entryName, runId));

            log.info("Committed entry: {}", entryName);

        } catch (Exception e) {
            log.error("Failed on entry '{}' — rolled back.", entryName, e);
        }
    }

    private void processOrSkip(ZipInputStream zis, String entryName, String runId) {
        if (alreadyProcessed(runId, entryName)) {
            log.info("Skipping already-processed entry in this run: {}", entryName);
            return;
        }

        processEntry(zis, entryName, runId);
    }

    private void processEntry(ZipInputStream zis, String entryName, String runId) {
        CopyContext.set(zis);

        try {
            dataMapper.copy(entryName);
            dataMapper.insertCheckpoint(runId, entryName);
        } finally {
            CopyContext.clear();
        }
    }

    private String startRun() {
        String runId = UUID.randomUUID().toString();
        log.info("Starting remote import run {}", runId);
        return runId;
    }

    private void finishRun(String runId) {
        log.info("Import run {} complete.", runId);
    }

    private Path validateFolder(String localPath) {
        Path folder = Path.of(localPath);

        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("local-path is not a directory: " + folder);
        }

        return folder;
    }

    private List<Path> listJsonFiles(Path folder) throws IOException {
        try (Stream<Path> stream = Files.list(folder)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".json")).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
        }
    }

    private boolean isJsonFile(String name) {
        return name.endsWith(".json");
    }

    private boolean alreadyProcessed(String runId, String entryName) {
        return dataMapper.getTimesCheckpointed(runId, entryName) > 0;
    }

    private ZipInputStream wrapAsZipInputStream(InputStream jsonStream, String entryName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            jsonStream.transferTo(zos);
            zos.closeEntry();
        }

        return new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()));
    }

    private HttpURLConnection openConnection() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URI(URL).toURL().openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        return conn;
    }

    private String buildParams() {
        return String.format(PARAMS_TEMPLATE, apiToken);
    }
}