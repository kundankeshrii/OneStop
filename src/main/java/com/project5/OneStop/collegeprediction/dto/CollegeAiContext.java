package com.project5.OneStop.collegeprediction.dto;

import lombok.Data;

import java.util.List;

@Data
public class CollegeAiContext {


    private CollegePredictionRequest student;

    private List<CollegeResult> colleges;
}