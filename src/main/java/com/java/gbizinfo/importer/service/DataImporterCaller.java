package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.model.scheduler.ActiveImportWindow;
import com.java.gbizinfo.importer.model.scheduler.ImportWindow;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class DataImporterCaller {

    private final ImportExecutionService importExecutionService;
    private final List<ImportWindow> windows;
    private final ZoneId zone;

    public DataImporterCaller(
            ImportExecutionService importExecutionService,
            @Value("${app.importer.zone:Europe/Chisinau}") ZoneId zone,
            @Value("${app.importer.windows}") String windowsProperty
    ) {
        this.importExecutionService = importExecutionService;
        this.zone = zone;
        this.windows = parseWindows(windowsProperty);
    }

    @Scheduled(fixedDelayString = "${app.importer.probe-delay-ms:30000}")
    public void scheduleImport() {
        ActiveImportWindow activeWindow = findActiveWindow(Instant.now());

        if (activeWindow == null) {
            return;
        }

        importExecutionService.tryRun(
                activeWindow.getWindowKey(),
                activeWindow.getWindowDate()
        );
    }

    private ActiveImportWindow findActiveWindow(Instant nowInstant) {
        ZonedDateTime now = nowInstant.atZone(zone);
        LocalTime currentTime = now.toLocalTime();
        LocalDate currentDate = now.toLocalDate();

        for (ImportWindow window : windows) {
            if (isInsideWindow(currentTime, window.getStart(), window.getEnd())) {
                LocalDate windowDate = resolveWindowDate(currentDate, currentTime, window);
                return new ActiveImportWindow(window.getKey(), windowDate);
            }
        }

        return null;
    }

    private boolean isInsideWindow(LocalTime now, LocalTime start, LocalTime end) {
        if (start.equals(end)) {
            return true;
        }

        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        }

        return now.isAfter(start) || now.isBefore(end);
    }

    private LocalDate resolveWindowDate(LocalDate currentDate, LocalTime now, ImportWindow window) {
        LocalTime start = window.getStart();
        LocalTime end = window.getEnd();

        if (start.isBefore(end) || start.equals(end)) {
            return currentDate;
        }

        if (now.isBefore(end)) {
            return currentDate.minusDays(1);
        }

        return currentDate;
    }

    private List<ImportWindow> parseWindows(String windowsProperty) {
        if (windowsProperty == null || windowsProperty.isBlank()) {
            throw new IllegalArgumentException("Property app.importer.windows must not be empty");
        }

        List<ImportWindow> result = new ArrayList<>();

        for (String part : splitByComma(windowsProperty)) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            result.add(parseSingleWindow(trimmed));
        }

        return List.copyOf(result);
    }

    private String[] splitByComma(String input) {
        return input.split(",");
    }

    private ImportWindow parseSingleWindow(String input) {
        String[] keyAndRange = splitKeyAndRange(input);

        String key = keyAndRange[0].trim();
        String rangePart = keyAndRange[1].trim();

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Window key must not be empty: " + input);
        }

        LocalTime[] times = parseTimeRange(rangePart);

        return new ImportWindow(key, times[0], times[1]);
    }

    private String[] splitKeyAndRange(String input) {
        String[] parts = input.split("=", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid window format: " + input);
        }

        return parts;
    }

    private LocalTime[] parseTimeRange(String rangePart) {
        String[] range = rangePart.split("-", 2);

        if (range.length != 2) {
            throw new IllegalArgumentException("Invalid time range: " + rangePart);
        }

        LocalTime start = parseTime(range[0]);
        LocalTime end = parseTime(range[1]);

        return new LocalTime[]{start, end};
    }

    private LocalTime parseTime(String value) {
        return LocalTime.parse(value.trim());
    }
}