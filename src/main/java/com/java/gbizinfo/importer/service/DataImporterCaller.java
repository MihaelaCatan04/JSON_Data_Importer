package com.java.gbizinfo.importer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataImporterCaller {
    DataImporter dataImporter = new DataImporter();

    @Scheduled(cron = "${app.database.cleanup-cron}")
    public void scheduleImport() throws Exception {
        log.info("Trigger Data Import task");
        dataImporter.importData();
    }

}
