package com.santosh.jobtracker.applications;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable=false)
    private String userId;

    @Column(nullable=false)
    private String company;

    @Column(nullable=false)
    private String role;

    @Column(nullable=false)
    private String status; // APPLIED, SCREEN, TECH, ONSITE, OFFER, REJECTED, WITHDRAWN

    private String jobUrl;
    private String location;
    private String salaryRange;

    private Instant appliedDate;
    private Instant lastStatusAt;

    @Column(length = 5000)
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (lastStatusAt == null) lastStatusAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public JobApplication() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getCompany() { return company; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public String getJobUrl() { return jobUrl; }
    public String getLocation() { return location; }
    public String getSalaryRange() { return salaryRange; }
    public Instant getAppliedDate() { return appliedDate; }
    public Instant getLastStatusAt() { return lastStatusAt; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setCompany(String company) { this.company = company; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    public void setLocation(String location) { this.location = location; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
    public void setAppliedDate(Instant appliedDate) { this.appliedDate = appliedDate; }
    public void setLastStatusAt(Instant lastStatusAt) { this.lastStatusAt = lastStatusAt; }
    public void setDescription(String description) { this.description = description; }
}
