package com.project5.OneStop.history.entity;

import com.project5.OneStop.auth.entity.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "history",
        indexes = {
                @Index(name = "idx_history_user_id", columnList = "user_id"),
                @Index(name = "idx_history_created_at", columnList = "created_at")
        }
)
@Data
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    /*
     * PLACEMENT
     * SCHOLARSHIP
     * COLLEGE_FINDER
     */
    @Column(
            nullable = false,
            length = 50
    )
    private String module;


    /*
     * Human-readable title shown in History UI.
     *
     * Example:
     * "Juspay - SDE"
     * "Engineering Scholarships"
     * "College Prediction"
     */
    @Column(
            nullable = false,
            length = 255
    )
    private String title;


    /*
     * Original request/input sent by the user.
     *
     * Stored as JSON text so each module can have
     * different request structures.
     */
    @Lob
    @Column(
            name = "request_data",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String requestData;


    /*
     * Generated module response.
     *
     * Also stored as JSON text because Placement,
     * Scholarship and College Finder have different
     * response structures.
     */
    @Lob
    @Column(
            name = "response_data",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String responseData;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


    /*
     * History automatically expires after 7 days.
     */
    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;


    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;

        expiresAt =
                now.plusDays(7);
    }
}