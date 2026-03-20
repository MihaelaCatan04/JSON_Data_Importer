package com.java.gbizinfo.importer.model.scheduler;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
public class ImportExecutionState {
    private String windowKey;
    private LocalDate windowDate;
    private boolean running;
    private boolean success;
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
}
