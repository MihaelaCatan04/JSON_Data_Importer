package com.java.gbizinfo.importer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GBizInfoJsonImporterApplication {

    static void main(String[] args) {
        SpringApplication.run(GBizInfoJsonImporterApplication.class, args);
    }
}
