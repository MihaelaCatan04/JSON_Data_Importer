package com.java.gbizinfo.importer.model.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ChangedCompany {
    private long companyId;
    private GbizCompany data;
    private OffsetDateTime newUpdatedAt;
}
