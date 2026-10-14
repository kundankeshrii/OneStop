package com.project5.OneStop.placement.controller;

import com.project5.OneStop.placement.dto.PlacementRequest;
import org.springframework.stereotype.Component;

@Component
public class PlacementPromptBuilder {

    public String buildPrompt(PlacementRequest request) {

        return """
                You are a placement preparation assistant.
                
                Create a personalized placement preparation plan for:
                
                Target Company: %s
                Target Role: %s
                Time Left: %d days
                
                Return ONLY valid JSON.
                Do not use Markdown.
                Do not use ```json.
                Do not add any explanation before or after the JSON.
                
                The JSON must contain EXACTLY these five top-level fields:
                
                1. skillsRequired
                2. hiringProcess
                3. previouslyAskedQuestions
                4. preparationPriority
                5. preparationRoadmap
                
                Use this exact JSON structure:
                
                {
                  "skillsRequired": [
                    {
                      "skill": "string",
                      "priority": "string",
                      "topics": "string"
                    }
                  ],
                
                  "hiringProcess": [
                    {
                      "stage": "string",
                      "focus": "string"
                    }
                  ],
                
                  "previouslyAskedQuestions": [
                    {
                      "question": "string",
                      "answer": "string"
                    }
                  ],
                
                  "preparationPriority": [
                    {
                      "rank": 1,
                      "area": "string",
                      "recommendedDays": 1
                    }
                  ],
                
                  "preparationRoadmap": [
                    {
                      "days": "1-3",
                      "focus": "string"
                    },
                    {
                      "days": "4-6",
                      "focus": "string"
                    }
                  ]
                }
                
                IMPORTANT RULES FOR preparationRoadmap:
                - Every preparationRoadmap element MUST contain both fields:
                  "days" and "focus".
                - "days" MUST be a non-empty string representing a day range
                  or a single day.
                - "focus" MUST be a non-empty string describing what the
                  student should prepare during those days.
                - NEVER return an empty object {} inside preparationRoadmap.
                - NEVER omit "days".
                - NEVER omit "focus".
                - Do not use fields such as "day", "topics", "tasks",
                  "activities", or "recommendedDays" inside preparationRoadmap.
                - Divide the complete %d-day preparation period into
                  logical day ranges.
                - The roadmap must cover the entire preparation period
                  from Day 1 through Day %d.
                
                IMPORTANT RULES FOR preparationPriority:
                - Every element MUST contain rank, area and recommendedDays.
                - The total of all recommendedDays MUST NOT exceed %d days.
                
                IMPORTANT RULES FOR previouslyAskedQuestions:
                
                - Return EXACTLY 15 question-answer objects.
                - The list MUST contain at least 10 objects.
                - The list MUST NOT contain more than 20 objects.
                - Prefer exactly 15 objects.
                
                - Every object MUST contain exactly two fields:
                  "question" and "answer".
                
                - "question" MUST contain only the interview question.
                - "answer" MUST contain a clear and technically correct answer
                  that a candidate can use during an interview.
                
                - Answers should be concise but sufficiently detailed.
                - Answers should be suitable for a fresher preparing for placement.
                - Include examples where they improve understanding.
                - Do not include Markdown formatting.
                - Do not include unnecessary introductions such as
                  "Sure", "Here is the answer", or "As an AI".
                - Do not include priority, frequency, internal scores,
                  difficulty or other metadata.
                
                - Questions must be relevant to the target company and target role.
                - Include a good mix of questions covering:
                  Java/OOP concepts,
                  data structures and algorithms,
                  SQL/DBMS,
                  Spring Boot/backend development,
                  REST APIs,
                  security where relevant,
                  problem-solving,
                  projects,
                  and HR/interview-stage topics relevant to the target role.
                
                - Answers must directly answer their corresponding questions.
                - Do not provide an answer to a different question.
                - Do not fabricate claims that a specific company definitely
                  asked a question unless the information is known.
                - Treat these as relevant previously-asked/common interview
                  preparation questions for the target company and role.
                
                Generate the content specifically for the target company
                and target role.
                
                ""\".formatted(
                    request.getTargetCompany(),
                    request.getTargetRole(),
                    request.getTimeLeftDays(),
                    request.getTimeLeftDays(),
                    request.getTimeLeftDays(),
                    request.getTimeLeftDays(),
                    request.getTimeLeftDays()
                );
                """.formatted(
                request.getTargetCompany(),
                request.getTargetRole(),
                request.getTimeLeftDays(),
                request.getTimeLeftDays(),
                request.getTimeLeftDays(),
                request.getTimeLeftDays()
        );
    }
}