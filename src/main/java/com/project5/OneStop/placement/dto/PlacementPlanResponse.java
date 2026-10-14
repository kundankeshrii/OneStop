package com.project5.OneStop.placement.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlacementPlanResponse {

    private List<SkillItems> skillsRequired;

    private List<HiringStage> hiringProcess;

    private List<PreviouslyAskedQuestion> previouslyAskedQuestions;

    private List<PriorityItem> preparationPriority;

    private List<RoadmapDay> preparationRoadmap;
}
