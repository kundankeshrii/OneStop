package com.project5.OneStop.collegeprediction.dto;

import lombok.Data;

@Data
public class CollegeRecommendation {

    private String college;
    private String branch;
    private String category;
    private String reason;
    private String confidence;
}