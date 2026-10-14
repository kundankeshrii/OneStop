package com.project5.OneStop.collegeprediction.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "college_cutoffs")
@Data
public class CollegeCutoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private String exam;

    private String counsellingAuthority;

    private String college;

    private String collegeType;

    private String state;

    private String course;

    private String branch;

    private String category;

    private String gender;

    private Boolean pwd;

    private String quota;

    /*
     * RANK
     * PERCENTILE
     * SCORE
     * MERIT
     */
    private String rankType;

    private Long openingRank;

    private Long closingRank;

    private Double openingPercentile;

    private Double closingPercentile;

    private Double openingScore;

    private Double closingScore;

    private String sourceName;

    private String dataSource;

    @Column(length = 1000)
    private String sourceUrl;
}