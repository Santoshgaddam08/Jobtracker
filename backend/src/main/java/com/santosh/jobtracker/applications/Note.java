package com.santosh.jobtracker.applications;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable=false)
    private String applicationId;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false, length=5000)
    private String body;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Note() {}

    public String getId() { return id; }
    public String getApplicationId() { return applicationId; }
    public String getUserId() { return userId; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }

    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBody(String body) { this.body = body; }
}
