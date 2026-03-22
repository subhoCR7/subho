package com.subho.energy;

import com.subho.energy.parser.Nem12Parser;
import com.subho.energy.util.SqlWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    void testDateParsing() {
        LocalDate date = LocalDate.parse("20050301", DateTimeFormatter.BASIC_ISO_DATE);
        assertEquals(2005, date.getYear());
        assertEquals(3, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test
    void sqlWriterUsesQuotedIdentifiersAndLocaleNeutralDecimals(@TempDir Path temp) throws Exception {
        Path out = temp.resolve("out.sql");
        try (SqlWriter w = new SqlWriter(out.toString())) {
            w.write("NMI1", LocalDate.parse("2005-03-01").atTime(0, 30), 1.25);
        }
        String sql = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(sql.contains("INSERT INTO meter_readings (\"nmi\", \"timestamp\", consumption)"));
        assertTrue(sql.contains("ON CONFLICT (\"nmi\", \"timestamp\") DO NOTHING"));
        assertTrue(sql.contains("1.250000") || sql.contains("1.25000"));
    }

    @Test
    void nem12ParserProducesFortyEightRowsForThirtyMinuteDay(@TempDir Path temp) throws Exception {
        Path in = temp.resolve("in.csv");
        Path out = temp.resolve("out.sql");
        Files.writeString(in, buildMinimalNem12(), StandardCharsets.UTF_8);

        new Nem12Parser().parse(in.toString(), out.toString());

        String sql = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(sql.contains("INSERT INTO meter_readings"));
        assertTrue(sql.contains("\"timestamp\""));
        assertTrue(sql.contains("NEM1201009"));
        assertEquals(48, countOccurrences(sql, "NEM1201009"));
        assertTrue(sql.contains("2005-03-01T00:30:00"));
        assertTrue(sql.contains("2005-03-02T00:00:00"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    private static String buildMinimalNem12() {
        StringBuilder sb = new StringBuilder();
        sb.append("100,NEM12,200506081149,UNITEDDP,NEMMCO\n");
        sb.append("200,NEM1201009,E1E2,1,E1,N1,01009,kWh,30,20050610\n");
        sb.append("300,20050301,");
        for (int i = 0; i < 48; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("1.0");
        }
        sb.append(",A,,,20050310121004,20050310182204\n900\n");
        return sb.toString();
    }
}
