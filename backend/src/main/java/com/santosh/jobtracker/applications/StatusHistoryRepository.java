package com.santosh.jobtracker.applications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, String> {
  List<StatusHistory> findByUserIdAndApplicationIdOrderByChangedAtDesc(String userId, String applicationId);

  long countByUserIdAndChangedAtAfter(String userId, Instant after);
}
