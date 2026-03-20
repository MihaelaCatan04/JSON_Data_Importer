package com.java.gbizinfo.importer.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CsvWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream(16 * 1024);

    public void writeRow(Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            writeValue(values[i]);
        }
        out.write('\n');
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }

    public int size() {
        return out.size();
    }

    private void writeValue(Object value) throws IOException {
        if (value == null) {
            return;
        }

        String s = value.toString();
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");

        if (!mustQuote) {
            out.write(s.getBytes(StandardCharsets.UTF_8));
            return;
        }

        out.write('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '"') {
                out.write('"');
                out.write('"');
            } else {
                out.write(String.valueOf(ch).getBytes(StandardCharsets.UTF_8));
            }
        }
        out.write('"');
    }
}