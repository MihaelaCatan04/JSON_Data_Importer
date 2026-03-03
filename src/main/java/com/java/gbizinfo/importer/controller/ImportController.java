package com.java.gbizinfo.importer.controller;

import com.java.gbizinfo.importer.service.DataImporter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class ImportController {
    private final DataImporter dataImporter;

    @Autowired
    public ImportController(DataImporter dataImporter) {
        this.dataImporter = dataImporter;
    }

    @GetMapping("/import")
    public String importData() {
        try {
            dataImporter.importData();
            return "Import successful!";
        } catch (Exception e) {
            log.error("Import failed", e);
            return "Import failed: " + e.getMessage();
        }
    }
}