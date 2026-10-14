package com.project5.OneStop.history.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class HistoryCleanupService {

    @Autowired
    private HistoryService historyService;


    /*
     * Runs every day at 2:00 AM.
     *
     * Deletes history records whose
     * expiresAt is before the current time.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredHistory() {

        historyService.deleteExpiredHistory();
    }
}