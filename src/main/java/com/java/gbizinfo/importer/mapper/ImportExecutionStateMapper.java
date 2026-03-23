package com.java.gbizinfo.importer.mapper;

import com.java.gbizinfo.importer.model.scheduler.ImportExecutionState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface ImportExecutionStateMapper {
    ImportExecutionState findByWindowKeyAndWindowDate(@Param("windowKey") String windowKey, @Param("windowDate") LocalDate windowDate);

    int insert(ImportExecutionState state);

    int update(ImportExecutionState state);
}
