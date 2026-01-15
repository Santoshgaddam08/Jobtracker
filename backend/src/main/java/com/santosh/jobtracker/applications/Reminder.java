package com.santosh.jobtracker.applications;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable=false)
    private String applicationId;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false)
    private String title;

    private Instant dueAt;

    @Column(nullable=false)
    private boolean sent;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Reminder() {}

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public Instant getDueAt() { return dueAt; }
    public boolean isSent() { return sent; }
    public Instant getCreatedAt() { return createdAt; }

    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }
    public void setSent(boolean sent) { this.sent = sent; }
}
