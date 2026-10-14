package com.project5.OneStop.common.ai;

import com.project5.OneStop.placement.dto.PlacementPlanResponse;
import com.project5.OneStop.placement.dto.RoadmapDay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class AiClient {

    @Autowired
    private RestClient groqRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${groq.model}")
    private String model;


    // ============================================================
    // PLACEMENT PLAN GENERATION
    // ============================================================

    public PlacementPlanResponse generate(String prompt) {

        /*
         * response_format = json_object tells Groq that this request
         * must return valid JSON instead of normal conversational text.
         *
         * temperature = 0.2 reduces unnecessary variation in the
         * generated structure.
         */

        Map<String, Object> requestBody = Map.of(

                "model", model,

                "temperature", 0.2,

                "response_format", Map.of(
                        "type", "json_object"
                ),

                "messages", new Object[]{

                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                }
        );


        try {

            // ====================================================
            // CALL GROQ
            // ====================================================

            String response = groqRestClient
                    .post()
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);


            // ====================================================
            // DEBUG: RAW GROQ RESPONSE
            // ====================================================

            System.out.println();
            System.out.println("========== GROQ RAW RESPONSE ==========");
            System.out.println(response);
            System.out.println("=======================================");
            System.out.println();


            // ====================================================
            // VALIDATE RESPONSE
            // ====================================================

            if (response == null || response.isBlank()) {

                throw new RuntimeException(
                        "Groq returned an empty response"
                );
            }


            // ====================================================
            // PARSE GROQ RESPONSE
            // ====================================================

            JsonNode root = objectMapper.readTree(response);


            // ====================================================
            // CHECK choices
            // ====================================================

            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {

                throw new RuntimeException(
                        "Groq response does not contain a valid 'choices' array"
                );
            }


            // ====================================================
            // GET message.content
            // ====================================================

            JsonNode contentNode = choices
                    .get(0)
                    .path("message")
                    .path("content");


            if (contentNode.isMissingNode()
                    || contentNode.isNull()
                    || contentNode.asText().isBlank()) {

                throw new RuntimeException(
                        "Groq response does not contain valid message.content"
                );
            }


            String content = contentNode.asText();


            // ====================================================
            // DEBUG: ACTUAL MODEL CONTENT
            // ====================================================

            System.out.println();
            System.out.println("========== GROQ CONTENT ==========");
            System.out.println(content);
            System.out.println("==================================");
            System.out.println();


            // ====================================================
            // CONVERT JSON -> PlacementPlanResponse
            // ====================================================

            PlacementPlanResponse result =
                    objectMapper.readValue(
                            content,
                            PlacementPlanResponse.class
                    );


            // ====================================================
            // DEBUG: AFTER DTO MAPPING
            // ====================================================

            System.out.println();
            System.out.println("===== ROADMAP AFTER MAPPING =====");


            if (result.getPreparationRoadmap() != null) {

                for (RoadmapDay roadmapDay :
                        result.getPreparationRoadmap()) {

                    System.out.println(
                            "Days: "
                                    + roadmapDay.getDays()
                                    + " | Focus: "
                                    + roadmapDay.getFocus()
                    );
                }

            } else {

                System.out.println(
                        "Preparation roadmap is NULL"
                );
            }


            System.out.println(
                    "================================"
            );
            System.out.println();


            return result;


        } catch (Exception e) {

            // ====================================================
            // DETAILED ERROR
            // ====================================================

            System.err.println();
            System.err.println("========== GROQ ERROR ==========");

            System.err.println(
                    "Model: " + model
            );

            System.err.println(
                    "Error Type: "
                            + e.getClass().getName()
            );

            System.err.println(
                    "Error Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "================================"
            );
            System.err.println();


            throw new RuntimeException(
                    "Failed to generate placement plan: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // ============================================================
    // NORMAL TEXT GENERATION
    // ============================================================

    public String generateText(String prompt) {

        Map<String, Object> requestBody = Map.of(

                "model", model,

                "temperature", 0.3,

                "messages", new Object[]{

                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                }
        );


        try {

            // ====================================================
            // CALL GROQ
            // ====================================================

            String response = groqRestClient
                    .post()
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);


            // ====================================================
            // DEBUG: RAW RESPONSE
            // ====================================================

            System.out.println();
            System.out.println("========== GROQ TEXT RESPONSE ==========");
            System.out.println(response);
            System.out.println("=========================================");
            System.out.println();


            if (response == null || response.isBlank()) {

                throw new RuntimeException(
                        "Groq returned an empty response"
                );
            }


            // ====================================================
            // PARSE RESPONSE
            // ====================================================

            JsonNode root =
                    objectMapper.readTree(response);


            // ====================================================
            // CHECK choices
            // ====================================================

            JsonNode choices =
                    root.path("choices");


            if (!choices.isArray() || choices.isEmpty()) {

                throw new RuntimeException(
                        "Groq response does not contain a valid 'choices' array"
                );
            }


            // ====================================================
            // GET message.content
            // ====================================================

            JsonNode contentNode =
                    choices
                            .get(0)
                            .path("message")
                            .path("content");


            if (contentNode.isMissingNode()
                    || contentNode.isNull()) {

                throw new RuntimeException(
                        "Groq response does not contain message.content"
                );
            }


            return contentNode.asText();


        } catch (Exception e) {

            // ====================================================
            // DETAILED ERROR
            // ====================================================

            System.err.println();
            System.err.println(
                    "========== GROQ TEXT ERROR =========="
            );

            System.err.println(
                    "Model: " + model
            );

            System.err.println(
                    "Error Type: "
                            + e.getClass().getName()
            );

            System.err.println(
                    "Error Message: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "====================================="
            );
            System.err.println();


            throw new RuntimeException(
                    "Failed to generate text: "
                            + e.getMessage(),
                    e
            );
        }
    }
}