package com.java.gbizinfo.importer.context;

import java.util.zip.ZipInputStream;

public final class CopyContext {

    private static final ThreadLocal<ZipInputStream> ZIP_HOLDER = new ThreadLocal<>();

    private CopyContext() {
    }

    public static void set(ZipInputStream zipInputStream) {
        ZIP_HOLDER.set(zipInputStream);
    }

    public static ZipInputStream getZip() {
        return ZIP_HOLDER.get();
    }

    public static void clear() {
        ZIP_HOLDER.remove();
    }
}