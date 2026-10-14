package com.project5.OneStop.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipRequest {

    private String state;
    private String category;
    private Double annualFamilyIncome;
    private String course;
    private String educationStage;

    private String currentYear;

    private String lastQualifyingExam;

    private Double lastQualifyingExamPercentage;
    private String gender;
    private Boolean pwd;
    private String parentOccupation;

}
