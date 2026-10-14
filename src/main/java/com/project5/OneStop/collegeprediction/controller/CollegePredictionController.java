package com.project5.OneStop.collegeprediction.controller;

import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.collegeprediction.dto.CollegePredictionRequest;
import com.project5.OneStop.collegeprediction.dto.CollegePredictionResponse;
import com.project5.OneStop.collegeprediction.service.CollegePredictionService;
import com.project5.OneStop.history.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/college-prediction")
@CrossOrigin("*")
public class CollegePredictionController {

    private final CollegePredictionService predictionService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ObjectMapper objectMapper;


    public CollegePredictionController(
            CollegePredictionService predictionService) {

        this.predictionService = predictionService;
    }


    @PostMapping("/predict")
    public ResponseEntity<CollegePredictionResponse> predict(
            @RequestBody CollegePredictionRequest request) {


        // =========================================================
        // EXISTING COLLEGE PREDICTION LOGIC
        // =========================================================

        CollegePredictionResponse response =
                predictionService.predict(request);


        // =========================================================
        // GET CURRENT AUTHENTICATION
        // =========================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // =========================================================
        // SAVE HISTORY ONLY FOR LOGGED-IN USERS
        // =========================================================

        if (authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof User) {

            try {

                User user =
                        (User) authentication.getPrincipal();


                String requestData =
                        objectMapper.writeValueAsString(
                                request
                        );


                String responseData =
                        objectMapper.writeValueAsString(
                                response
                        );


                historyService.saveHistory(
                        user,
                        "COLLEGE_FINDER",
                        "College Prediction",
                        requestData,
                        responseData
                );


                System.out.println(
                        "===== COLLEGE HISTORY SAVED ====="
                );


            } catch (Exception e) {

                /*
                 * History failure must NOT break
                 * College Finder functionality.
                 */
                System.err.println(
                        "College history save failed:"
                );

                e.printStackTrace();
            }
        }


        // =========================================================
        // EXISTING RESPONSE
        // =========================================================

        return ResponseEntity.ok(response);
    }
}