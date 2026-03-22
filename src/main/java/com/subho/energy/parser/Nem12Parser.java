package com.subho.energy.parser;

import com.subho.energy.util.SqlWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Nem12Parser {

    private static final Logger logger = Logger.getLogger(Nem12Parser.class.getName());

    public void parse(String input, String output) throws Exception {

        int totalRecords = 0;
        int skippedRecords = 0;

        try (BufferedReader br = new BufferedReader(
                     new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
             SqlWriter writer = new SqlWriter(output)) {

            String line;
            String nmi = null;
            int interval = 30;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] p = line.split(",", -1);

                if (!isValidRecord(p)) {
                    skippedRecords++;
                    continue;
                }

                if ("200".equals(p[0])) {
                    if (p.length < 9) {
                        skippedRecords++;
                        logger.warning("Skipping incomplete 200 record");
                        nmi = null;
                        continue;
                    }
                    try {
                        interval = Integer.parseInt(p[8].trim());
                    } catch (NumberFormatException e) {
                        skippedRecords++;
                        logger.warning("Invalid IntervalLength in 200 record");
                        nmi = null;
                        continue;
                    }
                    if (!isValidIntervalLength(interval)) {
                        logger.warning("Invalid IntervalLength " + interval
                                + " for NMI " + p[1] + "; skipping 300 records until next 200");
                        nmi = null;
                        continue;
                    }
                    nmi = p[1];
                    logger.log(Level.FINE, () -> "Processing NMI: " + nmi);
                }

                if ("300".equals(p[0]) && nmi != null) {

                    LocalDate date = LocalDate.parse(p[1].trim(), DateTimeFormatter.BASIC_ISO_DATE);
                    int total = 1440 / interval;

                    for (int i = 0; i < total && i + 2 < p.length; i++) {
                        try {
                            double c = Double.parseDouble(p[i + 2].trim());

                            LocalDateTime ts = date.atStartOfDay().plusMinutes((i + 1L) * interval);

                            writer.write(nmi, ts, c);
                            totalRecords++;

                        } catch (Exception e) {
                            skippedRecords++;
                            logger.warning("Skipping invalid value: " + p[i + 2]);
                        }
                    }
                }
            }
        }

        logger.info("Total processed records: " + totalRecords);
        logger.info("Skipped records: " + skippedRecords);
    }

    private static boolean isValidIntervalLength(int minutes) {
        return (minutes == 5 || minutes == 15 || minutes == 30) && 1440 % minutes == 0;
    }

    private boolean isValidRecord(String[] parts) {
        return parts != null && parts.length > 2 && parts[0] != null && !parts[0].isEmpty();
    }
}
