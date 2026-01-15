package com.santosh.jobtracker.scheduler;

import com.santosh.jobtracker.applications.Reminder;
import com.santosh.jobtracker.applications.ReminderRepository;
import com.santosh.jobtracker.realtime.RealtimePublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderScheduler {

    private final ReminderRepository repo;
    private final RealtimePublisher realtime;

    public ReminderScheduler(ReminderRepository repo, RealtimePublisher realtime) {
        this.repo = repo;
        this.realtime = realtime;
    }

    @Scheduled(fixedDelay = 30000) // every 30 seconds
    @Transactional
    public void sendDueReminders() {
        List<Reminder> due = repo.findBySentFalseAndDueAtBefore(Instant.now());
        for (Reminder r : due) {
            r.setSent(true);
            repo.save(r);
            realtime.publishUserEvent(r.getUserId(), "REMINDER_DUE", r);
        }
    }
}
