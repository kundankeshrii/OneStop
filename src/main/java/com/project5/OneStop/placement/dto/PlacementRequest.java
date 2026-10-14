package com.project5.OneStop.placement.dto;


import lombok.Data;

@Data
public class PlacementRequest {

    private String targetCompany;
    private String targetRole;
    private Integer timeLeftDays;

}
