package com.project5.OneStop.scholarship.controller;

import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.history.service.HistoryService;
import com.project5.OneStop.scholarship.dto.ScholarshipRequest;
import com.project5.OneStop.scholarship.dto.ScholarshipResponse;
import com.project5.OneStop.scholarship.service.ScholarshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/scholarships")
@CrossOrigin("*")
public class ScholarshipController {

    @Autowired
    private ScholarshipService scholarshipService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping("/find")
    public ResponseEntity<ScholarshipResponse> findScholarships(
            @RequestBody ScholarshipRequest request
    ) {

        // =========================================================
        // EXISTING SCHOLARSHIP LOGIC
        // =========================================================

        ScholarshipResponse response =
                scholarshipService.findScholarships(request);


        // =========================================================
        // GET CURRENT AUTHENTICATION
        // =========================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // =========================================================
        // SAVE HISTORY ONLY FOR LOGGED-IN USER
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


                /*
                 * Keep title generic for now because
                 * ScholarshipRequest fields haven't been
                 * provided here.
                 */
                String title =
                        "Scholarship Search";


                historyService.saveHistory(
                        user,
                        "SCHOLARSHIP",
                        title,
                        requestData,
                        responseData
                );


            } catch (Exception e) {

                /*
                 * History failure must NOT break
                 * Scholarship functionality.
                 */
                System.err.println(
                        "Scholarship history save failed:"
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