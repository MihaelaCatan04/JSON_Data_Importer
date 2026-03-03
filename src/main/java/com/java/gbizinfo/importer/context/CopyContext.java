package com.java.gbizinfo.importer.context;

import java.util.zip.ZipInputStream;

public class CopyContext {

    private static final ThreadLocal<ZipInputStream> ZIS = new ThreadLocal<>();

    public static void set(ZipInputStream zip) { ZIS.set(zip); }

    public static ZipInputStream getZip() { return ZIS.get(); }

    public static void clear() { ZIS.remove(); }
}