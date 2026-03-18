package com.java.gbizinfo.importer.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MergeMapper {

    void mergeCompany();

    void mergeItemInfo();

    void mergeCompanyItem();

    void mergeBaseInfo();

    void mergeWomenActivity();

    void mergeCompatChildcare();

    void truncateCompanyWorkplaceParts();

    void seedCompanyWorkplaceParts();

    void resolveBaseInfoToCompanyParts();

    void resolveWomenActivityToCompanyParts();

    void resolveChildcareToCompanyParts();

    void computeWorkplacePartsMergeKey();

    void mergeWorkplaceInfo();

    void attachWorkplaceToCompany();

    void mergePatent();

    void mergeCompanyPatent();

    void mergeClassification();

    void mergePatentClassification();

    void mergeCertification();

    void mergeCompanyCertification();

    void mergeSubsidy();

    void mergeCompanySubsidy();

    void mergeCommendation();

    void mergeCompanyCommendation();

    void mergeProcurement();

    void mergeCompanyProcurement();

    void mergeFinance();

    void mergeCompanyFinance();

    void mergeMajorShareholder();

    void mergeFinanceShareholder();

    void mergeManagementIndex();

    void mergeFinanceManagement();
}