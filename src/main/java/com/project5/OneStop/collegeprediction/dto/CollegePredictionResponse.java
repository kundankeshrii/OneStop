package com.project5.OneStop.collegeprediction.dto;

import lombok.Data;

import java.util.List;

@Data
public class CollegePredictionResponse {

    private String aiAnalysis;

    private List<CollegeResult> collegeResultList;

    private List<CollegeRecommendation> recommendations;

    private CollegeResult targetCollegeResult;

    private String targetCollegeStatus;

    private String targetCollegeMessage;
}