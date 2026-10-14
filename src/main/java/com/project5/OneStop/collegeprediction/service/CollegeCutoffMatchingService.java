package com.project5.OneStop.collegeprediction.service;

import com.project5.OneStop.collegeprediction.dto.CollegePredictionRequest;
import com.project5.OneStop.collegeprediction.entity.CollegeCutoff;
import com.project5.OneStop.collegeprediction.repository.CollegeCutoffRepository;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CollegeCutoffMatchingService {

    private final CollegeCutoffRepository repository;

    public CollegeCutoffMatchingService(
            CollegeCutoffRepository repository) {

        this.repository = repository;
    }

    public List<CollegeCutoff> getRelevantCutoffs(
            CollegePredictionRequest request) {

        int previousYear =
                Year.now().getValue() - 1;

        List<CollegeCutoff> allCutoffs =
                repository.findByYearAndCounsellingAuthority(
                        previousYear,
                        "JoSAA"
                );

        List<CollegeCutoff> filtered =
                allCutoffs.stream()
                        .filter(c -> matchesCategory(c, request))
                        .filter(c -> matchesGender(c, request))
                        .filter(c -> matchesPwd(c, request))
                        .filter(c -> matchesQuota(c, request))
                        .filter(c -> matchesCourse(c, request))
                        .collect(Collectors.toList());

        Long userRank =
                parseRank(request.getExamRankPercentile());

        List<CollegeCutoff> results =
                filtered.stream()
                        .collect(Collectors.toMap(
                                this::createUniqueKey,
                                c -> c,
                                this::mergeCutoff
                        ))
                        .values()
                        .stream()
                        .toList();

        if (userRank == null) {
            return results.stream()
                    .limit(20)
                    .toList();
        }

        final Long rank = userRank;

        return results.stream()
                .sorted(
                        Comparator
                                .comparingInt(
                                        (CollegeCutoff c) ->
                                                targetCollegeScore(
                                                        c,
                                                        request.getTargetCollege()
                                                )
                                )
                                .reversed()

                                .thenComparingInt(
                                        c -> rankPosition(
                                                c,
                                                rank
                                        )
                                )

                                .thenComparingLong(
                                        c -> distanceFromCutoff(
                                                c,
                                                rank
                                        )
                                )

                                .thenComparing(
                                        c -> safe(c.getCollege())
                                )
                )
                .limit(20)
                .toList();
    }

    private int rankPosition(
            CollegeCutoff cutoff,
            Long rank) {

        if (cutoff.getOpeningRank() == null ||
                cutoff.getClosingRank() == null) {

            return 3;
        }

        long opening =
                cutoff.getOpeningRank();

        long closing =
                cutoff.getClosingRank();

        /*
         * 0 = student's rank is inside
         *     previous-year cutoff range.
         */
        if (rank >= opening && rank <= closing) {
            return 0;
        }

        /*
         * 1 = student's rank is better
         *     than previous-year range.
         */
        if (rank < opening) {
            return 1;
        }

        /*
         * 2 = student's rank is worse
         *     than previous-year range.
         */
        return 2;
    }

    private long distanceFromCutoff(
            CollegeCutoff cutoff,
            Long rank) {

        if (cutoff.getOpeningRank() == null ||
                cutoff.getClosingRank() == null) {

            return Long.MAX_VALUE;
        }

        long opening =
                cutoff.getOpeningRank();

        long closing =
                cutoff.getClosingRank();

        if (rank >= opening && rank <= closing) {
            return 0;
        }

        if (rank < opening) {
            return opening - rank;
        }

        return rank - closing;
    }

    private int targetCollegeScore(
            CollegeCutoff cutoff,
            String targetCollege) {

        String target =
                safe(targetCollege);

        if (target.isBlank()) {
            return 0;
        }

        String college =
                safe(cutoff.getCollege());

        if (college.isBlank()) {
            return 0;
        }

        return college
                .toLowerCase(Locale.ROOT)
                .contains(
                        target.toLowerCase(Locale.ROOT)
                )
                ? 1
                : 0;
    }

    private boolean matchesCategory(
            CollegeCutoff cutoff,
            CollegePredictionRequest request) {

        String requested =
                safe(request.getCategory());

        if (requested.isBlank()) {
            return true;
        }

        String actual =
                safe(cutoff.getCategory());

        if (actual.isBlank()) {
            return false;
        }

        requested = normalize(requested);
        actual = normalize(actual);

        if (requested.equals("obc")) {
            return actual.contains("obc");
        }

        return actual.equals(requested);
    }

    private boolean matchesGender(
            CollegeCutoff cutoff,
            CollegePredictionRequest request) {

        String requested =
                safe(request.getGender());

        if (requested.isBlank()) {
            return true;
        }

        String actual =
                safe(cutoff.getGender());

        if (actual.isBlank()) {
            return true;
        }

        return actual.equalsIgnoreCase("Gender-Neutral")
                || actual.equalsIgnoreCase(requested);
    }

    private boolean matchesPwd(
            CollegeCutoff cutoff,
            CollegePredictionRequest request) {

        String requested =
                safe(request.getPwd());

        if (requested.isBlank()
                || requested.equalsIgnoreCase("No")
                || requested.equalsIgnoreCase("Not Applicable")) {

            return cutoff.getPwd() == null
                    || !cutoff.getPwd();
        }

        if (requested.equalsIgnoreCase("Yes")) {

            return cutoff.getPwd() != null
                    && cutoff.getPwd();
        }

        return true;
    }

    private boolean matchesQuota(
            CollegeCutoff cutoff,
            CollegePredictionRequest request) {

        String requested =
                safe(request.getQuota());

        if (requested.isBlank()
                || requested.equalsIgnoreCase("All")) {
            return true;
        }

        String actual =
                safe(cutoff.getQuota());

        if (actual.isBlank()) {
            return false;
        }

        return normalize(actual)
                .equals(normalize(requested));
    }

    private boolean matchesCourse(
            CollegeCutoff cutoff,
            CollegePredictionRequest request) {

        String requested =
                safe(request.getPreferredCourse());

        if (requested.isBlank()) {
            return true;
        }

        String actual =
                safe(cutoff.getBranch());

        if (actual.isBlank()) {
            return false;
        }

        requested = normalize(requested);
        actual = normalize(actual);

        if (requested.equals("cse")
                || requested.equals("cs")
                || requested.contains("computerscience")) {

            return actual.contains("computerscience");
        }

        if (requested.equals("ece")) {

            return actual.contains("electronics")
                    && actual.contains("communication");
        }

        if (requested.equals("eee")) {

            return actual.contains("electrical")
                    && actual.contains("electronics");
        }

        if (requested.equals("me")
                || requested.equals("mechanical")) {

            return actual.contains("mechanical");
        }

        if (requested.equals("ce")
                || requested.equals("civil")) {

            return actual.contains("civil");
        }

        return actual.contains(requested)
                || requested.contains(actual);
    }

    private Long parseRank(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(
                    value.trim()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String createUniqueKey(
            CollegeCutoff c) {

        return safe(c.getCollege()) + "|"
                + safe(c.getBranch()) + "|"
                + safe(c.getCategory()) + "|"
                + safe(c.getGender()) + "|"
                + safe(c.getQuota()) + "|"
                + String.valueOf(c.getPwd());
    }

    private CollegeCutoff mergeCutoff(
            CollegeCutoff first,
            CollegeCutoff second) {

        if (second.getOpeningRank() != null
                && (first.getOpeningRank() == null
                || second.getOpeningRank()
                < first.getOpeningRank())) {

            first.setOpeningRank(
                    second.getOpeningRank()
            );
        }

        if (second.getClosingRank() != null
                && (first.getClosingRank() == null
                || second.getClosingRank()
                > first.getClosingRank())) {

            first.setClosingRank(
                    second.getClosingRank()
            );
        }

        if (second.getOpeningPercentile() != null
                && (first.getOpeningPercentile() == null
                || second.getOpeningPercentile()
                < first.getOpeningPercentile())) {

            first.setOpeningPercentile(
                    second.getOpeningPercentile()
            );
        }

        if (second.getClosingPercentile() != null
                && (first.getClosingPercentile() == null
                || second.getClosingPercentile()
                > first.getClosingPercentile())) {

            first.setClosingPercentile(
                    second.getClosingPercentile()
            );
        }

        return first;
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private String normalize(String value) {

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    public List<CollegeCutoff> findTargetCollegeCutoffs(
            CollegePredictionRequest request) {

        int previousYear =
                Year.now().getValue() - 1;

        List<CollegeCutoff> allCutoffs =
                repository.findByYearAndCounsellingAuthority(
                        previousYear,
                        "JoSAA"
                );

        String target =
                safe(
                        request.getTargetCollege()
                );

        if (target.isBlank()) {
            return List.of();
        }

        return allCutoffs.stream()

                /*
                 * Target college name
                 */
                .filter(c ->
                        safe(c.getCollege())
                                .contains(target)
                )

                /*
                 * Same category
                 */
                .filter(c ->
                        matchesCategory(
                                c,
                                request
                        )
                )

                /*
                 * Same gender
                 */
                .filter(c ->
                        matchesGender(
                                c,
                                request
                        )
                )

                /*
                 * Same PwD status
                 */
                .filter(c ->
                        matchesPwd(
                                c,
                                request
                        )
                )

                /*
                 * Same quota
                 */
                .filter(c ->
                        matchesQuota(
                                c,
                                request
                        )
                )

                /*
                 * Same preferred branch/course
                 */
                .filter(c ->
                        matchesCourse(
                                c,
                                request
                        )
                )

                .collect(
                        Collectors.toList()
                );
    }
}