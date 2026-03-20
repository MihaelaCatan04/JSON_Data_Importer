package com.java.gbizinfo.importer.model.scheduler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
public class ImportWindow {
    private String key;
    private LocalTime start;
    private LocalTime end;
}