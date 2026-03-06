package com.java.gbizinfo.importer.context;

import java.util.zip.ZipInputStream;

public class CopyContext {

    private static final ThreadLocal<ZipInputStream> ZIS = new ThreadLocal<>();
    private static final ThreadLocal<Long> PATENT_SEQ = new ThreadLocal<>();
    private static final ThreadLocal<Long> WORKPLACE_SEQ = new ThreadLocal<>();
    private static final ThreadLocal<Long> FINANCE_SEQ = new ThreadLocal<>();
    private static final ThreadLocal<Long> COMPANY_SEQ = new ThreadLocal<>();

    public static void set(ZipInputStream zip) {
        ZIS.set(zip);
    }

    public static ZipInputStream getZip() {
        return ZIS.get();
    }

    public static long getPatentSeq() {
        if (PATENT_SEQ.get() == null) return 0L;
        return PATENT_SEQ.get();
    }

    public static void setPatentSeq(long seq) {
        PATENT_SEQ.set(seq);
    }

    public static long getWorkplaceSeq() {
        if (WORKPLACE_SEQ.get() == null) {
            return 0L;
        }
        return WORKPLACE_SEQ.get();
    }

    public static void setWorkplaceSeq(long seq) {
        WORKPLACE_SEQ.set(seq);
    }

    public static long getFinanceSeq() {
        if (FINANCE_SEQ.get() == null) {
            return 0L;
        }
        return FINANCE_SEQ.get();
    }

    public static void setFinanceSeq(long seq) {
        FINANCE_SEQ.set(seq);
    }

    public static long getCompanySeq() {
        if (COMPANY_SEQ.get() == null) {
            return 0L;
        }
        return FINANCE_SEQ.get();
    }

    public static void clear() {
        ZIS.remove();
        PATENT_SEQ.remove();
        WORKPLACE_SEQ.remove();
        FINANCE_SEQ.remove();
        COMPANY_SEQ.remove();
    }
}