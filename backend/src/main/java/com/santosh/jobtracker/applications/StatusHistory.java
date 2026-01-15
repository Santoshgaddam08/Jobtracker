package com.santosh.jobtracker.applications;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="status_history")
public class StatusHistory {
    long countByUserIdAndChangedAtAfter(String userId, Instant after);
    long countByUserIdAndApplicationIdAndChangedAtAfter(String userId, String applicationId, Instant after);


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable=false)
    private String applicationId;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false)
    private String fromStatus;

    @Column(nullable=false)
    private String toStatus;

    @Column(length=2000)
    private String note;

    @Column(nullable=false)
    private Instant changedAt;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) changedAt = Instant.now();
    }

    public StatusHistory() {}

    // Getters
    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getUserId() { return userId; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getNote() { return note; }
    public Instant getChangedAt() { return changedAt; }

    // Setters
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public void setNote(String note) { this.note = note; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
