package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.context.CopyContext;
import com.java.gbizinfo.importer.mapper.DataMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Log4j2
@Service
public class DataImporter {

    private static final String URL = "https://info.gbiz.go.jp/hojin/Download";
    private static final String PARAMS_TEMPLATE = "apiToken=%s&downfile=Hojinjoho&meta=&downenc=UTF-8&isZip=on&downtype=zip";

    @Value("${apiToken}")
    private String apiToken;

    @Autowired
    private DataMapper dataMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void importData() throws Exception {
        HttpURLConnection http = openConnection();
        http.getOutputStream().write(buildParams().getBytes(StandardCharsets.UTF_8));

        try (ZipInputStream zis = new ZipInputStream(http.getInputStream())) {
            selectJsonFiles(zis);
        } finally {
            http.disconnect();
        }
    }

    private void selectJsonFiles(ZipInputStream zis) throws IOException {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.endsWith(".json")) {
                beginTransaction(zis, name);
            } else {
                log.debug("Skipping non-JSON entry: {}", name);
            }
            zis.closeEntry();
        }
    }

    private void beginTransaction(ZipInputStream zis, String entryName) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                processOrSkip(zis, entryName);
            });
            log.info("Committed entry: {}", entryName);

        } catch (Exception e) {
            log.error("Failed on entry '{}' — rolled back. Will retry on next run.", entryName, e);
        }
    }

    private void processOrSkip(ZipInputStream zis, String entryName) {
        if (dataMapper.getTimesCheckpointed(entryName) > 0) {
            log.info("Skipping already-processed entry: {}", entryName);
            return;
        }
        processSingleEntry(zis, entryName);
    }

    private void processSingleEntry(ZipInputStream zis, String entryName) {
        CopyContext.set(zis);
        try {
            dataMapper.copy(entryName);
            dataMapper.insertCheckpoint(entryName);
        } finally {
            CopyContext.clear();
        }
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