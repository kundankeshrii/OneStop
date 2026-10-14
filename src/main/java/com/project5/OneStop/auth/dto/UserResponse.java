package com.project5.OneStop.auth.dto;

import com.project5.OneStop.auth.entity.AuthProvider;
import com.project5.OneStop.auth.entity.User;
import lombok.Data;

@Data
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private AuthProvider provider;
    private String profileImage;

    public UserResponse(User user) {

        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.provider = user.getProvider();
        this.profileImage = user.getProfileImage();
    }
}
