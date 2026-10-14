package com.project5.OneStop.collegeprediction.importer;

import com.project5.OneStop.collegeprediction.entity.CollegeCutoff;
import com.project5.OneStop.collegeprediction.repository.CollegeCutoffRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
public class CutoffCsvImporter {

    @Autowired
    private CollegeCutoffRepository collegeCutoffRepository;

    public int importCsv(
            MultipartFile file,
            String source) {

        int imported = 0;

        try (
                Reader reader = new InputStreamReader(
                        file.getInputStream(),
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

            for (CSVRecord row : parser) {

                CollegeCutoff cutoff =
                        new CollegeCutoff();

                cutoff.setYear(
                        parseInteger(row, "year")
                );

                cutoff.setExam(
                        value(row, "exam")
                );

                cutoff.setCounsellingAuthority(
                        source
                );

                cutoff.setCollege(
                        value(row, "college")
                );

                cutoff.setCollegeType(
                        value(row, "collegeType")
                );

                cutoff.setState(
                        value(row, "state")
                );

                cutoff.setCourse(
                        value(row, "course")
                );

                cutoff.setBranch(
                        value(row, "branch")
                );

                cutoff.setCategory(
                        value(row, "category")
                );

                cutoff.setGender(
                        value(row, "gender")
                );

                cutoff.setPwd(
                        parseBoolean(row, "pwd")
                );

                cutoff.setQuota(
                        value(row, "quota")
                );

                cutoff.setRankType(
                        value(row, "rankType")
                );

                cutoff.setOpeningRank(
                        parseLong(row, "openingRank")
                );

                cutoff.setClosingRank(
                        parseLong(row, "closingRank")
                );

                cutoff.setOpeningPercentile(
                        parseDouble(
                                row,
                                "openingPercentile"
                        )
                );

                cutoff.setClosingPercentile(
                        parseDouble(
                                row,
                                "closingPercentile"
                        )
                );

                cutoff.setOpeningScore(
                        parseDouble(
                                row,
                                "openingScore"
                        )
                );

                cutoff.setClosingScore(
                        parseDouble(
                                row,
                                "closingScore"
                        )
                );

                cutoff.setSourceName(
                        source.equalsIgnoreCase("JOSAA")
                                ? "JoSAA Official"
                                : "UPTAC Official"
                );

                cutoff.setSourceUrl(
                        source.equalsIgnoreCase("JOSAA")
                                ? "https://josaa.nic.in/or-cr/"
                                : "https://uptac.admissions.nic.in/document/b-tech-opening-and-closing-ranks-2025/"
                );

                collegeCutoffRepository.save(cutoff);

                imported++;
            }

            return imported;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to import " + source
                            + " cutoff data",
                    e
            );
        }
    }

    private String value(
            CSVRecord row,
            String column) {

        String value = row.get(column);

        return value == null
                ? null
                : value.trim();
    }

    private Integer parseInteger(
            CSVRecord row,
            String column) {

        String value = value(row, column);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Integer.parseInt(value);
    }

    private Long parseLong(
            CSVRecord row,
            String column) {

        String value = value(row, column);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Long.parseLong(value);
    }

    private Double parseDouble(
            CSVRecord row,
            String column) {

        String value = value(row, column);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Double.parseDouble(value);
    }

    private Boolean parseBoolean(
            CSVRecord row,
            String column) {

        String value = value(row, column);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Boolean.parseBoolean(value);
    }
}