package com.java.gbizinfo.importer.service;

import com.java.gbizinfo.importer.mapper.ImportExecutionStateMapper;
import com.java.gbizinfo.importer.model.scheduler.ImportExecutionState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class ImportExecutionTaskService {

    private final ImportExecutionStateMapper stateMapper;
    private final ZoneId zoneId;

    public ImportExecutionTaskService(ImportExecutionStateMapper stateMapper, @Value("${app.importer.zone:Europe/Chisinau}") ZoneId zoneId) {
        this.stateMapper = stateMapper;
        this.zoneId = zoneId;
    }

    @Transactional
    public ImportExecutionState getOrCreateState(String windowKey, LocalDate windowDate) {
        ImportExecutionState state = stateMapper.findByWindowKeyAndWindowDate(windowKey, windowDate);

        if (state != null) {
            return state;
        }

        try {
            stateMapper.insert(createInitialState(windowKey, windowDate));
        } catch (DuplicateKeyException ignored) {
        }

        return stateMapper.findByWindowKeyAndWindowDate(windowKey, windowDate);
    }

    @Transactional
    public boolean markRunning(String windowKey, LocalDate windowDate) {
        ImportExecutionState state = stateMapper.findByWindowKeyAndWindowDate(windowKey, windowDate);

        if (state == null || state.isRunning() || state.isSuccess()) {
            return false;
        }

        state.setRunning(true);
        state.setUpdatedAt(now());
        stateMapper.update(state);

        return true;
    }

    @Transactional
    public void markSuccess(String windowKey, LocalDate windowDate) {
        updateState(windowKey, windowDate, false, true);
    }

    @Transactional
    public void markFailure(String windowKey, LocalDate windowDate) {
        updateState(windowKey, windowDate, false, false);
    }

    private void updateState(String windowKey, LocalDate windowDate, boolean running, boolean success) {
        ImportExecutionState state = stateMapper.findByWindowKeyAndWindowDate(windowKey, windowDate);

        if (state == null) return;

        state.setRunning(running);
        state.setSuccess(success);
        state.setUpdatedAt(now());
        stateMapper.update(state);
    }

    private ImportExecutionState createInitialState(String windowKey, LocalDate windowDate) {
        ImportExecutionState state = new ImportExecutionState();
        state.setWindowKey(windowKey);
        state.setWindowDate(windowDate);
        state.setRunning(false);
        state.setSuccess(false);
        state.setInsertedAt(now());
        return state;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(zoneId);
    }
}