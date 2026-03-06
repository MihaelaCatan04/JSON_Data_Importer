package com.java.gbizinfo.importer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DataMapper {
    int getTimesCheckpointed(@Param("entryName") String entryName);

    int copy(@Param("entryName") String entryName);

    void insertCheckpoint(@Param("entryName") String entryName);

    long getMaxPatentId();

    long getMaxWorkplaceInfoId();

    long getMaxFinanceId();

    long getMaxCompanyId();
}