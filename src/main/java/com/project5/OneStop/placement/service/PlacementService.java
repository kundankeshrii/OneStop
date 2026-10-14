package com.project5.OneStop.placement.service;

import com.project5.OneStop.common.ai.AiClient;
import com.project5.OneStop.placement.controller.PlacementPromptBuilder;
import com.project5.OneStop.placement.dto.PlacementPlanResponse;
import com.project5.OneStop.placement.dto.PlacementRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlacementService {

    @Autowired
    private PlacementPromptBuilder promptBuilder;

    @Autowired
    private AiClient aiClient;

    public PlacementPlanResponse generatePlan(PlacementRequest request) {

        String prompt = promptBuilder.buildPrompt(request);

        return aiClient.generate(prompt);
    }
}