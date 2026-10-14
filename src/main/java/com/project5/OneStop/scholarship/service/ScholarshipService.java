package com.project5.OneStop.scholarship.service;

import com.project5.OneStop.scholarship.dto.ScholarshipRequest;
import com.project5.OneStop.scholarship.dto.ScholarshipResponse;
import com.project5.OneStop.scholarship.dto.ScholarshipResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class ScholarshipService {

    @Autowired
    private RestClient groqRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${groq.model}")
    private String model;


    // ============================================================
    // FIND SCHOLARSHIPS
    // ============================================================

    public ScholarshipResponse findScholarships(
            ScholarshipRequest request) {

        validateRequest(request);

        String prompt = buildPrompt(request);

        Map<String, Object> requestBody = Map.of(

                "model", model,

                "messages", new Object[]{
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                },

                // Force Groq to return JSON
                "response_format", Map.of(
                        "type", "json_object"
                )
        );


        try {

            // ----------------------------------------------------
            // CALL GROQ
            // ----------------------------------------------------

            String response = groqRestClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);


            // ----------------------------------------------------
            // PARSE GROQ RESPONSE
            // ----------------------------------------------------

            JsonNode root =
                    objectMapper.readTree(response);


            JsonNode choices =
                    root.path("choices");


            if (!choices.isArray()
                    || choices.isEmpty()) {

                throw new RuntimeException(
                        "Invalid AI response"
                );
            }


            String content =
                    choices
                            .get(0)
                            .path("message")
                            .path("content")
                            .asText();


            if (content == null
                    || content.isBlank()) {

                throw new RuntimeException(
                        "AI returned an empty response"
                );
            }


            // ----------------------------------------------------
            // EXTRACT ACTUAL JSON
            // ----------------------------------------------------
            // Protects against responses such as:
            //
            // "Based on your profile..."
            // {
            //    "scholarshipResultList": [...]
            // }
            //
            // or accidental ```json formatting.
            // ----------------------------------------------------

            content = extractJson(content);


            // ----------------------------------------------------
            // JSON → ScholarshipResponse
            // ----------------------------------------------------

            ScholarshipResponse result =
                    objectMapper.readValue(
                            content,
                            ScholarshipResponse.class
                    );


            // ----------------------------------------------------
            // VALIDATE RESULT
            // ----------------------------------------------------

            validateScholarshipResults(result);


            return result;


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to process scholarship response",
                    e
            );
        }
    }


    // ============================================================
    // EXTRACT JSON FROM AI RESPONSE
    // ============================================================

    private String extractJson(String content) {

        content = content.trim();


        // Remove markdown code fences if present

        if (content.startsWith("```json")) {

            content = content
                    .substring(7)
                    .trim();
        }

        else if (content.startsWith("```")) {

            content = content
                    .substring(3)
                    .trim();
        }


        if (content.endsWith("```")) {

            content = content
                    .substring(
                            0,
                            content.length() - 3
                    )
                    .trim();
        }


        // --------------------------------------------------------
        // Find actual JSON object
        // --------------------------------------------------------

        int start =
                content.indexOf("{");

        int end =
                content.lastIndexOf("}");


        if (start == -1
                || end == -1
                || start > end) {

            throw new RuntimeException(
                    "AI response does not contain valid JSON"
            );
        }


        return content.substring(
                start,
                end + 1
        );
    }


    // ============================================================
    // VALIDATE SCHOLARSHIP RESULTS
    // ============================================================

    private void validateScholarshipResults(
            ScholarshipResponse result) {

        if (result == null) {

            throw new RuntimeException(
                    "Invalid scholarship response"
            );
        }


        if (result.getScholarshipResultList() == null) {

            result.setScholarshipResultList(
                    List.of()
            );

            return;
        }


        // Maximum 5 scholarships

        if (result.getScholarshipResultList().size() > 5) {

            result.setScholarshipResultList(
                    result.getScholarshipResultList()
                            .subList(0, 5)
            );
        }


        for (ScholarshipResult scholarship :
                result.getScholarshipResultList()) {


            if (scholarship == null) {

                throw new RuntimeException(
                        "Invalid scholarship result"
                );
            }


            if (scholarship.getName() == null
                    || scholarship.getName().isBlank()) {

                throw new RuntimeException(
                        "Scholarship name is missing"
                );
            }


            if (scholarship.getProvider() == null
                    || scholarship.getProvider().isBlank()) {

                throw new RuntimeException(
                        "Scholarship provider is missing"
                );
            }


            if (scholarship.getMatchScore() == null
                    || scholarship.getMatchScore() < 0
                    || scholarship.getMatchScore() > 100) {

                throw new RuntimeException(
                        "Invalid scholarship match score"
                );
            }


            // ========================================================
            // NORMALIZE STATUS
            // ========================================================

            String status = scholarship.getStatus();

            if (status == null || status.isBlank()) {

                throw new RuntimeException(
                        "Scholarship status is missing"
                );
            }


            status = status.trim();


            if (status.equalsIgnoreCase("Eligible")) {

                scholarship.setStatus("Eligible");

            } else if (
                    status.equalsIgnoreCase(
                            "Potentially Eligible"
                    )
            ) {

                scholarship.setStatus(
                        "Potentially Eligible"
                );

            } else if (
                    status.equalsIgnoreCase(
                            "Additional Verification Required"
                    )
            ) {

                scholarship.setStatus(
                        "Additional Verification Required"
                );

            } else {

                throw new RuntimeException(
                        "Invalid scholarship status: "
                                + status
                );
            }
        }
    }


    // ============================================================
    // BUILD AI PROMPT
    // ============================================================

    private String buildPrompt(
            ScholarshipRequest request) {

        return """

                You are a STRICT scholarship eligibility matching engine.

                Your task is NOT to generate generic scholarship suggestions.

                Your task is to identify ONLY scholarships for which this
                student's provided information actually matches the published
                eligibility criteria.

                ============================================================
                STUDENT PROFILE
                ============================================================

                State / Domicile:
                %s

                Category:
                %s

                Annual Family Income:
                ₹%s

                Course:
                %s

                Current Education Stage:
                %s

                Current Year:
                %s

                Last Qualifying Exam:
                %s

                Last Qualifying Exam Percentage:
                %s%%

                Gender:
                %s

                PwD Status:
                %s

                Parent Occupation:
                %s


                ============================================================
                STRICT MATCHING RULES
                ============================================================

                1. Check EVERY student field against the scholarship's
                   eligibility requirements.

                2. DO NOT return a scholarship simply because it is
                   generally suitable for students.

                3. State/domicile requirements must match whenever the
                   scholarship is restricted to a particular state.

                4. Category requirements must match exactly.

                   Example:
                   An OBC student must NOT receive an SC-only or ST-only
                   scholarship.

                5. Education stage must match.

                   Example:
                   A Class 11 student must NOT receive a scholarship that
                   is exclusively for undergraduate or postgraduate students.

                6. Course restrictions must match whenever the scholarship
                   has a specific course requirement.

                7. Income limits must be checked.

                   If the student's income is above the scholarship's
                   maximum income limit, DO NOT return that scholarship.

                8. Gender restrictions must be checked.

                   A male student must NOT receive a female-only scholarship.

                9. PwD restrictions must be checked.

                   A non-PwD student must NOT receive a PwD-only scholarship.

                10. Academic requirements must be checked.

                    Compare the student's last qualifying examination
                    percentage with the scholarship's minimum percentage
                    requirement whenever such a requirement exists.

                11. Parent occupation requirements must be checked whenever
                    the scholarship specifically requires a particular
                    occupation or family background.

                12. If ANY mandatory eligibility condition clearly fails,
                    EXCLUDE the scholarship completely.

                13. Do NOT return clearly ineligible scholarships as
                    "Potentially Eligible".

                14. "Potentially Eligible" may ONLY be used when the
                    student's known information matches but one important
                    eligibility condition cannot be determined.

                15. Do NOT manufacture scholarships to reach five results.

                16. It is completely acceptable to return fewer than five.

                17. If no scholarship matches the student, return:

                {
                  "scholarshipResultList": []
                }

                18. Do not invent scholarship names.

                19. Do not invent scholarship amounts.

                20. Do not invent eligibility requirements.

                21. Do not invent deadlines.

                22. Do not fabricate application links.

                23. Prefer official application portals.

                24. If the current deadline cannot be reliably established,
                    return an empty string for deadline.

                25. If an official application link cannot be reliably
                    established, return an empty string for applicationLink.

                26. Do not use old or expired information as current.

                27. Return ONLY valid JSON.

                28. Do not include markdown.

                29. Do not include explanatory text before or after JSON.


                ============================================================
                RESULT ORDER
                ============================================================

                Rank results by:

                1. Exact eligibility match
                2. Number of eligibility criteria satisfied
                3. Academic requirement match
                4. Income eligibility
                5. State/category relevance


                ============================================================
                MATCH SCORE
                ============================================================

                Match score must be between 0 and 100.

                The score represents how strongly the student's profile
                matches the scholarship's actual eligibility criteria.

                Do NOT give a high score merely because a scholarship is
                generally relevant.

                If a mandatory criterion fails, exclude the scholarship.


                ============================================================
                STATUS
                ============================================================

                Use ONLY:

                Eligible
                Potentially Eligible
                Additional Verification Required

                Use "Eligible" only when all known mandatory requirements
                are satisfied.

                Use "Potentially Eligible" only when the known requirements
                match but an important requirement cannot be determined.

                Do NOT return clearly ineligible scholarships.


                ============================================================
                OUTPUT
                ============================================================

                Return maximum 5 scholarships.

                Return fewer than 5 if fewer than 5 genuinely match.

                Return exactly this JSON structure:

                {
                  "scholarshipResultList": [
                    {
                      "name": "Scholarship Name",
                      "provider": "Scholarship Provider",
                      "eligibility": "Specific eligibility requirements that match this student",
                      "amount": "Scholarship amount or empty string if unknown",
                      "deadline": "Current deadline or empty string if unknown",
                      "matchScore": 92,
                      "status": "Eligible",
                      "reason": "Specific explanation of why the scholarship matches",
                      "applicationLink": "Official application URL or empty string"
                    }
                  ]
                }

                """.formatted(

                request.getState(),

                request.getCategory(),

                request.getAnnualFamilyIncome(),

                request.getCourse(),

                request.getEducationStage(),

                request.getCurrentYear(),

                request.getLastQualifyingExam(),

                request.getLastQualifyingExamPercentage(),

                request.getGender(),

                request.getPwd(),

                request.getParentOccupation()
        );
    }


    // ============================================================
    // REQUEST VALIDATION
    // ============================================================

    private void validateRequest(
            ScholarshipRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Scholarship request cannot be empty"
            );
        }


        if (isBlank(request.getState())
                || isBlank(request.getCategory())
                || request.getAnnualFamilyIncome() == null
                || isBlank(request.getEducationStage())
                || isBlank(request.getCurrentYear())
                || isBlank(request.getLastQualifyingExam())
                || request.getLastQualifyingExamPercentage() == null
                || isBlank(request.getGender())
                || request.getPwd() == null
                || isBlank(request.getParentOccupation())) {

            throw new IllegalArgumentException(
                    "All scholarship fields are required"
            );
        }


        if (request.getAnnualFamilyIncome() < 0) {

            throw new IllegalArgumentException(
                    "Annual family income cannot be negative"
            );
        }


        validatePercentage(
                request.getLastQualifyingExamPercentage(),
                "Last qualifying exam percentage"
        );
    }


    // ============================================================
    // PERCENTAGE VALIDATION
    // ============================================================

    private void validatePercentage(
            Double percentage,
            String fieldName) {

        if (percentage < 0
                || percentage > 100) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be between 0 and 100"
            );
        }
    }


    // ============================================================
    // BLANK CHECK
    // ============================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}