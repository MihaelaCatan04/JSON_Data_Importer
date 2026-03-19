package com.java.gbizinfo.importer.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SingleEntryZipInputStream extends ZipInputStream {

    private final InputStream delegate;
    private final String entryName;
    private boolean opened = false;
    private boolean closed = false;

    public SingleEntryZipInputStream(InputStream delegate, String entryName) {
        super(InputStream.nullInputStream());
        this.delegate = delegate;
        this.entryName = entryName;
    }

    @Override
    public ZipEntry getNextEntry() {
        if (opened || closed) {
            return null;
        }
        opened = true;
        return new ZipEntry(entryName);
    }

    @Override
    public void closeEntry() {
        closed = true;
    }

    @Override
    public int read() throws IOException {
        if (!opened || closed) {
            return -1;
        }
        return delegate.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!opened || closed) {
            return -1;
        }
        return delegate.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}