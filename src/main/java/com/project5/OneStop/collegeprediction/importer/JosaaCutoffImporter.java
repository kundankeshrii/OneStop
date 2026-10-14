package com.project5.OneStop.collegeprediction.importer;

import com.project5.OneStop.collegeprediction.entity.CollegeCutoff;
import com.project5.OneStop.collegeprediction.repository.CollegeCutoffRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Service
public class JosaaCutoffImporter {

    private final CollegeCutoffRepository repository;

    public JosaaCutoffImporter(CollegeCutoffRepository repository) {
        this.repository = repository;
    }

    public int importPreviousYear() {

        int previousYear = Year.now().getValue() - 1;

        Map<String, CollegeCutoff> aggregated = new HashMap<>();

        try {

            InputStream inputStream =
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream(
                                    "data/josaa_cutoffs.csv"
                            );

            if (inputStream == null) {
                throw new RuntimeException(
                        "JoSAA CSV not found at " +
                                "src/main/resources/data/josaa_cutoffs.csv"
                );
            }

            try (
                    Reader reader =
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            );

                    CSVParser parser =
                            CSVFormat.DEFAULT.builder()
                                    .setHeader()
                                    .setSkipHeaderRecord(true)
                                    .setIgnoreEmptyLines(true)
                                    .setTrim(true)
                                    .build()
                                    .parse(reader)
            ) {

                for (CSVRecord record : parser) {

                    String yearValue =
                            record.get("Year");

                    if (!String.valueOf(previousYear)
                            .equals(yearValue)) {
                        continue;
                    }

                    String institute =
                            record.get("Institute");

                    String program =
                            record.get("Academic Program Name");

                    String quota =
                            record.get("Quota");

                    String seatType =
                            record.get("Seat Type");

                    String gender =
                            record.get("Gender");

                    Long openingRank =
                            parseRank(
                                    record.get("Opening Rank")
                            );

                    Long closingRank =
                            parseRank(
                                    record.get("Closing Rank")
                            );

                    if (isBlank(institute)
                            || isBlank(program)
                            || isBlank(quota)
                            || isBlank(seatType)
                            || isBlank(gender)
                            || openingRank == null
                            || closingRank == null) {

                        continue;
                    }

                    boolean pwd =
                            seatType.contains("(PwD)");

                    String category =
                            seatType
                                    .replace("(PwD)", "")
                                    .trim();

                    String normalizedQuota =
                            normalizeQuota(quota);

                    String branch =
                            extractBranch(program);

                    String exam =
                            determineExam(institute);

                    String key =
                            institute + "|" +
                                    program + "|" +
                                    normalizedQuota + "|" +
                                    category + "|" +
                                    gender + "|" +
                                    pwd;

                    CollegeCutoff existing =
                            aggregated.get(key);

                    if (existing == null) {

                        CollegeCutoff cutoff =
                                new CollegeCutoff();

                        cutoff.setYear(previousYear);
                        cutoff.setExam(exam);
                        cutoff.setCounsellingAuthority("JoSAA");

                        cutoff.setCollege(institute);
                        cutoff.setCollegeType(
                                determineCollegeType(institute)
                        );

                        cutoff.setState(null);

                        cutoff.setCourse("B.Tech");
                        cutoff.setBranch(branch);

                        cutoff.setCategory(category);
                        cutoff.setGender(gender);
                        cutoff.setPwd(pwd);
                        cutoff.setQuota(normalizedQuota);

                        cutoff.setRankType("RANK");

                        cutoff.setOpeningRank(openingRank);
                        cutoff.setClosingRank(closingRank);

                        cutoff.setOpeningPercentile(null);
                        cutoff.setClosingPercentile(null);

                        cutoff.setOpeningScore(null);
                        cutoff.setClosingScore(null);

                        cutoff.setSourceName(
                                "JoSAA " + previousYear + " Dataset"
                        );

                        aggregated.put(key, cutoff);

                    } else {

                        if (openingRank <
                                existing.getOpeningRank()) {

                            existing.setOpeningRank(
                                    openingRank
                            );
                        }

                        if (closingRank >
                                existing.getClosingRank()) {

                            existing.setClosingRank(
                                    closingRank
                            );
                        }
                    }
                }
            }

            repository.deleteByYearAndCounsellingAuthority(
                    previousYear,
                    "JoSAA"
            );

            repository.saveAll(
                    aggregated.values()
            );

            return aggregated.size();

        }catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to import JoSAA previous-year data: "
                            + e.getMessage(),
                    e
            );
        }

    }

    private Long parseRank(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return (long) Double.parseDouble(
                    value.trim()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractBranch(String program) {

        if (program == null || program.isBlank()) {
            return null;
        }

        int index = program.indexOf(" (");

        if (index > 0) {
            return program
                    .substring(0, index)
                    .trim();
        }

        return program.trim();
    }

    private String normalizeQuota(String quota) {

        return switch (quota.trim()) {

            case "AI" -> "All India";

            case "HS" -> "Home State";

            case "OS" -> "Other State";

            default -> quota.trim();
        };
    }

    private String determineExam(String institute) {

        String name =
                institute.toLowerCase();

        if (name.contains(
                "indian institute of technology")) {

            return "JEE Advanced";
        }

        return "JEE Main";
    }

    private String determineCollegeType(
            String institute) {

        String name =
                institute.toLowerCase();

        if (name.contains(
                "indian institute of technology")
                || name.contains(
                "national institute of technology")
                || name.contains(
                "indian institute of information technology")) {

            return "Government";
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}