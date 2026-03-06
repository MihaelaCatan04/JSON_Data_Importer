package com.java.gbizinfo.importer.model.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ExistingCompany {
    private long companyId;
    private OffsetDateTime updatedAt;
}
