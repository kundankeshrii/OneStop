package com.project5.OneStop.collegeprediction.service;

import com.project5.OneStop.collegeprediction.dto.CollegeAiContext;
import com.project5.OneStop.collegeprediction.dto.CollegePredictionRequest;
import com.project5.OneStop.collegeprediction.dto.CollegePredictionResponse;
import com.project5.OneStop.collegeprediction.dto.CollegeRecommendation;
import com.project5.OneStop.collegeprediction.dto.CollegeResult;
import com.project5.OneStop.collegeprediction.entity.CollegeCutoff;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollegePredictionService {

    private final CollegeCutoffMatchingService matchingService;
    private final CollegeAiService aiService;

    public CollegePredictionService(
            CollegeCutoffMatchingService matchingService,
            CollegeAiService aiService) {

        this.matchingService = matchingService;
        this.aiService = aiService;
    }

    public CollegePredictionResponse predict(
            CollegePredictionRequest request) {

        validateExamAndCourse(request);

        List<CollegeCutoff> relevantCutoffs =
                matchingService.getRelevantCutoffs(request);

        List<CollegeResult> collegeResults =
                relevantCutoffs.stream()
                        .limit(20)
                        .map(this::convert)
                        .toList();

        CollegePredictionResponse response =
                new CollegePredictionResponse();

        /*
         * =====================================================
         * TARGET COLLEGE
         * =====================================================
         */

        if (request.getTargetCollege() != null &&
                !request.getTargetCollege().isBlank()) {

            List<CollegeCutoff> targetCutoffs =
                    matchingService.findTargetCollegeCutoffs(
                            request
                    );

            if (targetCutoffs.isEmpty()) {

                response.setTargetCollegeStatus(
                        "NOT_FOUND"
                );

                response.setTargetCollegeMessage(
                        "The requested target college was not found in the available previous-year cutoff data for the selected course and preferences."
                );

            } else {

                CollegeCutoff target =
                        targetCutoffs.get(0);

                CollegeResult targetResult =
                        convert(target);

                response.setTargetCollegeResult(
                        targetResult
                );

                String status =
                        classifyTarget(
                                request,
                                target
                        );

                response.setTargetCollegeStatus(
                        status
                );

                response.setTargetCollegeMessage(
                        buildTargetMessage(
                                request,
                                target,
                                status
                        )
                );
            }
        }

        /*
         * =====================================================
         * AI RECOMMENDATIONS
         * =====================================================
         */

        CollegeAiContext context =
                new CollegeAiContext();

        context.setStudent(request);
        context.setColleges(collegeResults);

        List<CollegeRecommendation> recommendations =
                aiService.analyze(context);

        /*
         * =====================================================
         * FINAL RESPONSE
         * =====================================================
         */

        response.setCollegeResultList(
                collegeResults
        );

        response.setRecommendations(
                recommendations
        );

        return response;
    }

    private CollegeResult convert(
            CollegeCutoff cutoff) {

        CollegeResult result =
                new CollegeResult();

        result.setCollege(
                cutoff.getCollege()
        );

        result.setBranch(
                cutoff.getBranch()
        );

        result.setOpeningClosingRank(
                buildRankRange(cutoff)
        );

        result.setCounselling(
                cutoff.getCounsellingAuthority()
        );

        result.setFees(
                "Research required"
        );

        result.setAvgPackage(
                "Research required"
        );

        return result;
    }

    private String buildRankRange(
            CollegeCutoff cutoff) {

        if (cutoff.getOpeningRank() == null &&
                cutoff.getClosingRank() == null) {

            return "Not available";
        }

        if (cutoff.getOpeningRank() == null) {

            return String.valueOf(
                    cutoff.getClosingRank()
            );
        }

        if (cutoff.getClosingRank() == null) {

            return String.valueOf(
                    cutoff.getOpeningRank()
            );
        }

        return cutoff.getOpeningRank()
                + " – "
                + cutoff.getClosingRank();
    }

    /*
     * =========================================================
     * TARGET CLASSIFICATION
     * =========================================================
     */

    private String classifyTarget(
            CollegePredictionRequest request,
            CollegeCutoff cutoff) {

        if (!"RANK".equalsIgnoreCase(
                request.getRankType())) {

            return "DATA_AVAILABLE";
        }

        Long studentRank =
                parseRank(
                        request.getExamRankPercentile()
                );

        if (studentRank == null) {
            return "DATA_AVAILABLE";
        }

        if (cutoff.getOpeningRank() == null ||
                cutoff.getClosingRank() == null) {

            return "DATA_AVAILABLE";
        }

        long opening =
                cutoff.getOpeningRank();

        long closing =
                cutoff.getClosingRank();

        if (studentRank >= opening &&
                studentRank <= closing) {

            return "TARGET";
        }

        if (studentRank < opening) {

            return "SAFE";
        }

        return "AMBITIOUS";
    }

    private String buildTargetMessage(
            CollegePredictionRequest request,
            CollegeCutoff cutoff,
            String status) {

        String range =
                buildRankRange(cutoff);

        Long rank =
                parseRank(
                        request.getExamRankPercentile()
                );

        if (rank == null) {

            return "Previous-year cutoff data is available for this target college.";
        }

        return switch (status) {

            case "SAFE" ->
                    "Your rank " +
                            rank +
                            " is better than the previous-year cutoff range " +
                            range +
                            ".";

            case "TARGET" ->
                    "Your rank " +
                            rank +
                            " falls within the previous-year cutoff range " +
                            range +
                            ".";

            case "AMBITIOUS" ->
                    "Your rank " +
                            rank +
                            " is weaker than the previous-year cutoff range " +
                            range +
                            ". This is an ambitious option based on historical data.";

            default ->
                    "Previous-year cutoff data is available for this target college.";
        };
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

    /*
     * =========================================================
     * EXAM VALIDATION
     * =========================================================
     */

    private void validateExamAndCourse(
            CollegePredictionRequest request) {

        String exam =
                safe(
                        request.getExamType()
                );

        String course =
                safe(
                        request.getPreferredCourse()
                );

        /*
         * Current database contains JoSAA
         * engineering cutoff data.
         */

        if (exam.equals("neet")) {

            throw new IllegalArgumentException(
                    "NEET is not supported for this college prediction module. Please select JEE Main or another supported engineering entrance exam."
            );
        }

        if (!exam.equals("jee main") &&
                !exam.equals("jee advanced")) {

            throw new IllegalArgumentException(
                    "The selected exam is not supported by the current college cutoff database."
            );
        }

        if (course.contains("mbbs") ||
                course.contains("bds") ||
                course.contains("medical") ||
                course.contains("dentistry")) {

            throw new IllegalArgumentException(
                    "Medical courses cannot be predicted using the current JoSAA engineering cutoff database."
            );
        }
    }

    private String safe(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase();
    }
}