package com.java.gbizinfo.importer.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StagingMapper {

    void clearStagingTables();

    void clearRunStagingTables();

    void appendRunCompany();

    void appendRunCompanyItem();

    void appendRunPatent();

    void appendRunClassification();

    void appendRunCertification();

    void appendRunSubsidy();

    void appendRunCommendation();

    void appendRunProcurement();

    void appendRunFinance();

    void appendRunMajorShareholder();

    void appendRunManagementIndex();
}