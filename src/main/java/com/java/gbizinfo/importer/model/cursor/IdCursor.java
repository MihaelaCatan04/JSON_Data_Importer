package com.java.gbizinfo.importer.model.cursor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class IdCursor {
    private final List<Long> ids;
    private int pos = 0;

    public IdCursor(List<Long> ids) {
        this.ids = ids;
    }

    public long moveNext() {
        return ids.get(pos++);
    }
}