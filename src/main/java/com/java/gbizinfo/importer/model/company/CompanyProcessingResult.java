package com.java.gbizinfo.importer.model.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class CompanyProcessingResult {
    List<GbizCompany> newCompanies;
    List<ChangedCompany> changedCompanies;
    int skipped;
}
