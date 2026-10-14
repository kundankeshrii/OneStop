package com.project5.OneStop.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipResult {
    private String name;
    private String provider;
    private String eligibility;
    private String amount;
    private String deadline;
    private Integer matchScore;
    private String status;
    private String reason;
    private String applicationLink;
}
