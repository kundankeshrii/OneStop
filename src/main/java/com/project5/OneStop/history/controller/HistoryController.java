package com.project5.OneStop.history.controller;

import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.history.dto.HistoryResponse;
import com.project5.OneStop.history.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;


    /*
     * Get logged-in user's history.
     */
    @GetMapping
    public ResponseEntity<?> getHistory(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated");
        }


        User user =
                (User) authentication.getPrincipal();


        List<HistoryResponse> history =
                historyService.getUserHistory(user);


        return ResponseEntity.ok(history);
    }


    /*
     * Delete one history item.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHistory(
            @PathVariable Long id,
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not authenticated");
        }


        User user =
                (User) authentication.getPrincipal();


        historyService.deleteHistory(
                id,
                user
        );


        return ResponseEntity.ok(
                "History deleted successfully"
        );
    }
}