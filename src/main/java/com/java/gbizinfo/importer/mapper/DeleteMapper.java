package com.java.gbizinfo.importer.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeleteMapper {

    void deleteStaleCompanyItems();

    void deleteStaleCompanyPatents();

    void deleteStalePatentClassifications();

    void deleteStaleCertifications();

    void deleteStaleSubsidies();

    void deleteStaleCommendations();

    void deleteStaleProcurements();

    void deleteStaleFinances();

    void deleteStaleFinanceShareholders();

    void deleteStaleFinanceManagement();
}