package com.project5.OneStop.history.dto;

import com.project5.OneStop.history.entity.History;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryResponse {

    private Long id;

    private String module;

    private String title;

    private String requestData;

    private String responseData;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;


    public HistoryResponse(History history) {

        this.id = history.getId();

        this.module = history.getModule();

        this.title = history.getTitle();

        this.requestData = history.getRequestData();

        this.responseData = history.getResponseData();

        this.createdAt = history.getCreatedAt();

        this.expiresAt = history.getExpiresAt();
    }
}