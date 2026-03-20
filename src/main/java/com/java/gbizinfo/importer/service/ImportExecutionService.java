package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.model.scheduler.ImportExecutionState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@Service
public class ImportExecutionService {

    private final DataImporter dataImporter;
    private final ImportExecutionTaskService taskService;
    private final String source;
    private final ZoneId zoneId;

    public ImportExecutionService(DataImporter dataImporter, ImportExecutionTaskService taskService, @Value("${app.importer.source}") String source, @Value("${app.importer.zone:Europe/Chisinau}") ZoneId zoneId) {
        this.dataImporter = dataImporter;
        this.taskService = taskService;
        this.source = source;
        this.zoneId = zoneId;
    }

    public void tryRun(String windowKey, LocalDate windowDate) {
        ImportExecutionState state = taskService.getOrCreateState(windowKey, windowDate);

        if (state == null || state.isSuccess()) {
            return;
        }

        if (state.isRunning()) {
            if (state.getUpdatedAt() != null && state.getUpdatedAt().isBefore(LocalDateTime.now(zoneId).minusMinutes(30))) {
                log.warn("Found stale running execution. Resetting it.");
                taskService.markFailure(windowKey, windowDate);
            } else {
                log.info("Execution is already running. Skipping.");
                return;
            }
        }


        if (!taskService.markRunning(windowKey, windowDate)) {
            return;
        }

        runImport(windowKey, windowDate);
    }

    private void runImport(String windowKey, LocalDate windowDate) {
        try {
            executeImport();
            taskService.markSuccess(windowKey, windowDate);
            log.info("Execution finished successfully.");
        } catch (Exception e) {
            taskService.markFailure(windowKey, windowDate);
            log.warn("Execution could not be finished", e);
        }
    }

    private void executeImport() throws Exception {
        if ("remote".equals(source)) {
            dataImporter.importData();
        } else if ("local".equals(source)) {
            dataImporter.importFromLocalFolder();
        } else {
            throw new IllegalArgumentException("Unknown source type: " + source);
        }
    }
}