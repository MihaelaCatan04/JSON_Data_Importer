package com.java.gbizinfo.importer.model.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
public class ActiveImportWindow {
    private String windowKey;
    private LocalDate windowDate;
}
