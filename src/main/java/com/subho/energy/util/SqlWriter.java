package com.subho.energy.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Locale;

public class SqlWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private final StringBuilder batch = new StringBuilder();
    private int count = 0;
    private static final int LIMIT = 500;

    public SqlWriter(String file) throws Exception {
        writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
    }

    public void write(String nmi, LocalDateTime ts, double c) throws Exception {

        batch.append(String.format(Locale.US, "('%s','%s',%f),", nmi, ts, c));
        count++;

        if (count >= LIMIT) flush();
    }

    private void flush() throws Exception {
        if (count == 0) return;

        writer.write("INSERT INTO meter_readings (\"nmi\", \"timestamp\", consumption) VALUES "
                + batch.substring(0, batch.length() - 1)
                + " ON CONFLICT (\"nmi\", \"timestamp\") DO NOTHING;");
        writer.newLine();

        batch.setLength(0);
        count = 0;
    }

    public void close() throws Exception {
        flush();
        writer.flush();
        writer.close();
    }
}
