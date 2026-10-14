package com.project5.OneStop.collegeprediction.dto;

import lombok.Data;

@Data
public class CollegePredictionRequest {

    private Double tenthPercentage;
    private Double twelfthPercentage;
    private Double diplomaPercentage;

    private String examType;
    private String examRankPercentile;
    private String rankType;

    private String gender;
    private String category;
    private String pwd;

    private String preferredCourse;

    private String quota;

    private String collegeType;
    private String targetCollege;

}
