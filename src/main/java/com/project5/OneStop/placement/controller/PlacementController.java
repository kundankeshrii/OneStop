package com.project5.OneStop.placement.controller;

import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.auth.repository.UserRepository;
import com.project5.OneStop.history.service.HistoryService;
import com.project5.OneStop.placement.dto.PlacementPlanResponse;
import com.project5.OneStop.placement.dto.PlacementRequest;
import com.project5.OneStop.placement.service.PlacementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/placement")
public class PlacementController {

    @Autowired
    private PlacementService placementService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping("/generate")
    public ResponseEntity<PlacementPlanResponse> generatePlan(
            @RequestBody PlacementRequest request
    ) {

        // =========================================================
        // EXISTING PLACEMENT LOGIC
        // =========================================================

        PlacementPlanResponse response =
                placementService.generatePlan(request);


        // =========================================================
        // GET CURRENT AUTHENTICATION DIRECTLY
        // =========================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        System.out.println(
                "===== PLACEMENT AUTH CHECK ====="
        );

        System.out.println(
                "Authentication: " + authentication
        );


        // =========================================================
        // SAVE HISTORY ONLY FOR LOGGED-IN USER
        // =========================================================

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof User) {

            try {

                User user =
                        (User) authentication.getPrincipal();


                System.out.println(
                        "Logged-in user: "
                                + user.getEmail()
                );


                String requestData =
                        objectMapper.writeValueAsString(
                                request
                        );


                String responseData =
                        objectMapper.writeValueAsString(
                                response
                        );


                String title =
                        request.getTargetCompany()
                                + " - "
                                + request.getTargetRole();


                historyService.saveHistory(
                        user,
                        "PLACEMENT",
                        title,
                        requestData,
                        responseData
                );


                System.out.println(
                        "===== PLACEMENT HISTORY SAVED ====="
                );

            } catch (Exception e) {

                System.err.println(
                        "===== PLACEMENT HISTORY FAILED ====="
                );

                e.printStackTrace();
            }

        } else {

            System.out.println(
                    "===== PLACEMENT GUEST USER ====="
            );
        }


        // =========================================================
        // EXISTING RESPONSE
        // =========================================================

        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );
    }
}