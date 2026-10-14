package com.project5.OneStop.history.service;

import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.history.dto.HistoryResponse;
import com.project5.OneStop.history.entity.History;
import com.project5.OneStop.history.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;


    /*
     * Save a new history record.
     */
    public History saveHistory(
            User user,
            String module,
            String title,
            String requestData,
            String responseData
    ) {

        History history = new History();

        history.setUser(user);

        history.setModule(module);

        history.setTitle(title);

        history.setRequestData(requestData);

        history.setResponseData(responseData);

        return historyRepository.save(history);
    }


    /*
     * Get only the authenticated user's
     * non-expired history.
     */
    public List<HistoryResponse> getUserHistory(
            User user
    ) {

        return historyRepository
                .findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(
                        user.getId(),
                        LocalDateTime.now()
                )
                .stream()
                .map(HistoryResponse::new)
                .toList();
    }


    /*
     * Delete one history item.
     *
     * IMPORTANT:
     * We verify that the history belongs
     * to the authenticated user.
     */
    public void deleteHistory(
            Long historyId,
            User user
    ) {

        History history =
                historyRepository.findById(historyId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "History not found"
                                )
                        );


        if (!history.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this history"
            );
        }


        historyRepository.delete(history);
    }


    /*
     * Delete all expired history.
     */
    public void deleteExpiredHistory() {

        historyRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }
}