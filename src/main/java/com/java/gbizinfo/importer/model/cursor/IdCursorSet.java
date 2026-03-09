package com.java.gbizinfo.importer.model.cursor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class IdCursorSet {
    IdCursor company;
    IdCursor patent;
    IdCursor workplace;
    IdCursor finance;
}
