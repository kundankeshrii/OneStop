package com.project5.OneStop.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;

    private Long userId;

    private String name;

    private String email;

    public AuthResponse(
            String token,
            Long userId,
            String name,
            String email
    ) {

        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}