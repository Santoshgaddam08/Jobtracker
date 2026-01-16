package com.santosh.jobtracker.applications;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_history")
public class StatusHistory {

  @Id
  private String id = UUID.randomUUID().toString();

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String applicationId;

  @Column(nullable = false)
  private String fromStatus;

  @Column(nullable = false)
  private String toStatus;

  private String note;

  @Column(nullable = false)
  private Instant changedAt;

  public StatusHistory() {}

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }

  public String getApplicationId() { return applicationId; }
  public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

  public String getFromStatus() { return fromStatus; }
  public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

  public String getToStatus() { return toStatus; }
  public void setToStatus(String toStatus) { this.toStatus = toStatus; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public Instant getChangedAt() { return changedAt; }
  public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
