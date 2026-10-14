package com.project5.OneStop.auth.controller;

import com.project5.OneStop.auth.dto.AuthResponse;
import com.project5.OneStop.auth.dto.LoginRequest;
import com.project5.OneStop.auth.dto.SignupRequest;
import com.project5.OneStop.auth.dto.UserResponse;
import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequest request
            ){
        User user=authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registed Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
            ){
        AuthResponse response =authService.login(request);

        return ResponseEntity.ok(response);

    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication
    ){

        if(authentication == null ||
                !authentication.isAuthenticated()){

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated");
        }

        User user = (User) authentication.getPrincipal();

        UserResponse response =
                new UserResponse(user);

        return ResponseEntity.ok(response);
    }
}
