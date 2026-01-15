package com.santosh.jobtracker.applications;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, String> {
    List<Reminder> findByUserIdAndApplicationIdOrderByDueAtAsc(String userId, String applicationId);
    List<Reminder> findBySentFalseAndDueAtBefore(Instant now);
}
