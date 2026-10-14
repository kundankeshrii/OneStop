package com.project5.OneStop.collegeprediction.service;

import com.project5.OneStop.collegeprediction.dto.CollegeAiContext;
import com.project5.OneStop.collegeprediction.dto.CollegeRecommendation;
import com.project5.OneStop.collegeprediction.dto.CollegeResult;
import com.project5.OneStop.common.ai.AiClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollegeAiService {

    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public CollegeAiService(
            AiClient aiClient,
            ObjectMapper objectMapper) {

        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    public List<CollegeRecommendation> analyze(
            CollegeAiContext context) {

        try {

            Long studentRank =
                    parseRank(
                            context.getStudent()
                                    .getExamRankPercentile()
                    );

            if (studentRank == null ||
                    context.getColleges() == null ||
                    context.getColleges().isEmpty()) {

                return List.of();
            }

            /*
             * Create ALL recommendations first.
             * Category is calculated by Java.
             */
            List<CollegeRecommendation> all =
                    context.getColleges()
                            .stream()
                            .map(college ->
                                    createRecommendation(
                                            college,
                                            studentRank
                                    )
                            )
                            .filter(Objects::nonNull)
                            .toList();

            /*
             * Separate by category.
             */
            List<CollegeRecommendation> safe =
                    all.stream()
                            .filter(r ->
                                    "Safe".equals(
                                            r.getCategory()
                                    )
                            )
                            .toList();

            List<CollegeRecommendation> target =
                    all.stream()
                            .filter(r ->
                                    "Target".equals(
                                            r.getCategory()
                                    )
                            )
                            .toList();

            List<CollegeRecommendation> ambitious =
                    all.stream()
                            .filter(r ->
                                    "Ambitious".equals(
                                            r.getCategory()
                                    )
                            )
                            .toList();

            /*
             * Balanced selection:
             *
             * 2 Safe
             * 2 Target
             * 1 Ambitious
             *
             * If one category has fewer options,
             * fill from the other categories.
             */
            List<CollegeRecommendation> result =
                    new java.util.ArrayList<>();

            addRecommendations(
                    result,
                    safe,
                    2
            );

            addRecommendations(
                    result,
                    target,
                    2
            );

            addRecommendations(
                    result,
                    ambitious,
                    1
            );

            /*
             * Fill remaining positions if a category
             * did not contain enough colleges.
             */
            if (result.size() < 5) {

                addRemaining(
                        result,
                        target,
                        5
                );

                addRemaining(
                        result,
                        safe,
                        5
                );

                addRemaining(
                        result,
                        ambitious,
                        5
                );
            }

            /*
             * Ask Groq ONLY for explanation text.
             * Java continues to control:
             *
             * college
             * branch
             * category
             * confidence
             */
            String recommendationJson =
                    objectMapper.writeValueAsString(
                            result
                    );

            String prompt = """
                You are the explanation layer of
                the OneStop College Prediction system.

                The backend has already calculated every
                recommendation.

                DO NOT change:
                - college
                - branch
                - category
                - confidence

                Generate only a short reason for each
                recommendation.

                Use the exact college names supplied.

                Return ONLY valid JSON.

                Format:

                {
                  "reasons": [
                    {
                      "college": "exact college name",
                      "reason": "short useful explanation"
                    }
                  ]
                }

                RECOMMENDATIONS:
                %s
                """.formatted(
                    recommendationJson
            );

            Map<String, String> reasons =
                    generateReasons(prompt);

            for (CollegeRecommendation recommendation :
                    result) {

                String reason =
                        reasons.get(
                                recommendation.getCollege()
                        );

                if (reason != null &&
                        !reason.isBlank()) {

                    recommendation.setReason(
                            reason
                    );
                }
            }

            return result;

        } catch (Exception e) {

            /*
             * AI failure must not break prediction.
             */
            try {

                Long studentRank =
                        parseRank(
                                context.getStudent()
                                        .getExamRankPercentile()
                        );

                if (studentRank == null) {
                    return List.of();
                }

                List<CollegeRecommendation> all =
                        context.getColleges()
                                .stream()
                                .map(c ->
                                        createRecommendation(
                                                c,
                                                studentRank
                                        )
                                )
                                .filter(Objects::nonNull)
                                .toList();

                return createBalancedSelection(
                        all
                );

            } catch (Exception ignored) {

                return List.of();
            }
        }
    }


    private void addRecommendations(
            List<CollegeRecommendation> result,
            List<CollegeRecommendation> source,
            int count) {

        for (CollegeRecommendation recommendation :
                source) {

            if (result.size() >= 5 ||
                    count <= 0) {

                break;
            }

            if (!result.contains(recommendation)) {

                result.add(recommendation);
                count--;
            }
        }
    }

    private void addRemaining(
            List<CollegeRecommendation> result,
            List<CollegeRecommendation> source,
            int max) {

        if (result.size() >= max) {
            return;
        }

        for (CollegeRecommendation recommendation :
                source) {

            if (result.size() >= max) {
                break;
            }

            if (!result.contains(recommendation)) {
                result.add(recommendation);
            }
        }
    }

    private List<CollegeRecommendation> createBalancedSelection(
            List<CollegeRecommendation> all) {

        List<CollegeRecommendation> safe =
                all.stream()
                        .filter(r ->
                                "Safe".equals(
                                        r.getCategory()
                                )
                        )
                        .toList();

        List<CollegeRecommendation> target =
                all.stream()
                        .filter(r ->
                                "Target".equals(
                                        r.getCategory()
                                )
                        )
                        .toList();

        List<CollegeRecommendation> ambitious =
                all.stream()
                        .filter(r ->
                                "Ambitious".equals(
                                        r.getCategory()
                                )
                        )
                        .toList();

        List<CollegeRecommendation> result =
                new java.util.ArrayList<>();

        addRecommendations(result, safe, 2);
        addRecommendations(result, target, 2);
        addRecommendations(result, ambitious, 1);

        if (result.size() < 5) {
            addRemaining(result, target, 5);
            addRemaining(result, safe, 5);
            addRemaining(result, ambitious, 5);
        }

        return result;
    }
    private CollegeRecommendation createRecommendation(
            CollegeResult college,
            Long studentRank) {

        RankRange range =
                parseRankRange(
                        college.getOpeningClosingRank()
                );

        if (range == null) {
            return null;
        }

        String category =
                classifyRank(
                        studentRank,
                        range.opening,
                        range.closing
                );

        CollegeRecommendation recommendation =
                new CollegeRecommendation();

        /*
         * EXACT values from database result.
         */
        recommendation.setCollege(
                college.getCollege()
        );

        recommendation.setBranch(
                college.getBranch()
        );

        /*
         * Calculated by Java.
         */
        recommendation.setCategory(
                category
        );

        recommendation.setConfidence(
                calculateConfidence(
                        studentRank,
                        range.opening,
                        range.closing
                )
        );

        recommendation.setReason(
                buildReason(
                        studentRank,
                        range.opening,
                        range.closing,
                        category
                )
        );

        return recommendation;
    }

    private String classifyRank(
            long studentRank,
            long opening,
            long closing) {

        /*
         * Student rank is better than the
         * previous-year opening rank.
         *
         * Example:
         *
         * Student = 200
         * Opening = 227
         *
         * 200 is better -> Safe
         */
        if (studentRank < opening) {
            return "Safe";
        }

        /*
         * Student rank is inside the
         * previous-year cutoff range.
         */
        if (studentRank <= closing) {
            return "Target";
        }

        /*
         * Student rank is worse than
         * previous-year closing rank.
         */
        return "Ambitious";
    }

    private String calculateConfidence(
            long studentRank,
            long opening,
            long closing) {

        long range =
                Math.max(1, closing - opening);

        if (studentRank < opening) {

            long margin =
                    opening - studentRank;

            if (margin >= range) {
                return "High";
            }

            return "Medium";
        }

        if (studentRank <= closing) {

            long distanceFromClosing =
                    closing - studentRank;

            if (distanceFromClosing <= range / 3) {
                return "High";
            }

            return "Medium";
        }

        long distance =
                studentRank - closing;

        if (distance <= range) {
            return "Medium";
        }

        return "Low";
    }

    private String buildReason(
            long studentRank,
            long opening,
            long closing,
            String category) {

        if ("Safe".equals(category)) {

            return "Student rank "
                    + studentRank
                    + " is better than the previous-year opening rank "
                    + opening
                    + ".";
        }

        if ("Target".equals(category)) {

            return "Student rank "
                    + studentRank
                    + " falls within the previous-year cutoff range of "
                    + opening
                    + "–"
                    + closing
                    + ".";
        }

        return "Student rank "
                + studentRank
                + " is worse than the previous-year closing rank "
                + closing
                + ".";
    }

    private int categoryPriority(
            String category) {

        if ("Target".equalsIgnoreCase(category)) {
            return 0;
        }

        if ("Safe".equalsIgnoreCase(category)) {
            return 1;
        }

        return 2;
    }

    private Map<String, String> generateReasons(
            String prompt) {

        Map<String, String> reasons =
                new HashMap<>();

        try {

            String response =
                    aiClient.generateText(prompt);

            String cleaned =
                    cleanJsonResponse(response);

            JsonNode root =
                    objectMapper.readTree(cleaned);

            JsonNode reasonsNode =
                    root.path("reasons");

            if (!reasonsNode.isArray()) {
                return reasons;
            }

            for (JsonNode node : reasonsNode) {

                String college =
                        node.path("college")
                                .asText();

                String reason =
                        node.path("reason")
                                .asText();

                if (!college.isBlank()
                        && !reason.isBlank()) {

                    reasons.put(
                            college,
                            reason
                    );
                }
            }

        } catch (Exception ignored) {
            /*
             * Deterministic reasons remain available.
             */
        }

        return reasons;
    }

    private RankRange parseRankRange(
            String value) {

        if (value == null ||
                value.isBlank() ||
                value.equalsIgnoreCase("Not available")) {

            return null;
        }

        String cleaned =
                value.replaceAll(
                        "[^0-9]+",
                        " "
                ).trim();

        String[] parts =
                cleaned.split("\\s+");

        if (parts.length < 2) {
            return null;
        }

        try {

            long opening =
                    Long.parseLong(parts[0]);

            long closing =
                    Long.parseLong(parts[1]);

            return new RankRange(
                    opening,
                    closing
            );

        } catch (NumberFormatException e) {

            return null;
        }
    }

    private Long parseRank(
            String value) {

        if (value == null ||
                value.isBlank()) {

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

    private String cleanJsonResponse(
            String response) {

        if (response == null ||
                response.isBlank()) {

            throw new RuntimeException(
                    "Groq returned an empty response"
            );
        }

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned.substring(7).trim();

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned.substring(
                            0,
                            cleaned.length() - 3
                    ).trim();
        }

        int start =
                cleaned.indexOf('{');

        int end =
                cleaned.lastIndexOf('}');

        if (start == -1 ||
                end == -1 ||
                end <= start) {

            throw new RuntimeException(
                    "Invalid Groq JSON response"
            );
        }

        return cleaned.substring(
                start,
                end + 1
        ).trim();
    }

    private static class RankRange {

        private final long opening;
        private final long closing;

        private RankRange(
                long opening,
                long closing) {

            this.opening = opening;
            this.closing = closing;
        }
    }
}