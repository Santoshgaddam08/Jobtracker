package com.santosh.jobtracker.applications;

import com.santosh.jobtracker.realtime.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository appRepo;
    private final StatusHistoryRepository historyRepo;
    private final NoteRepository noteRepo;
    private final ReminderRepository reminderRepo;
    private final RealtimePublisher realtimePublisher;

    public JobApplicationService(JobApplicationRepository appRepo,
                                 StatusHistoryRepository historyRepo,
                                 NoteRepository noteRepo,
                                 ReminderRepository reminderRepo,
                                 RealtimePublisher realtimePublisher) {
        this.appRepo = appRepo;
        this.historyRepo = historyRepo;
        this.noteRepo = noteRepo;
        this.reminderRepo = reminderRepo;
        this.realtimePublisher = realtimePublisher;
    }

    public List<JobApplication> list(String userId) {
        return appRepo.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public ApplicationDtos.DetailResponse detail(String userId, String id) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        return new ApplicationDtos.DetailResponse(
                app,
                historyRepo.findByUserIdAndApplicationIdOrderByChangedAtDesc(userId, id),
                noteRepo.findByUserIdAndApplicationIdOrderByCreatedAtDesc(userId, id),
                reminderRepo.findByUserIdAndApplicationIdOrderByDueAtAsc(userId, id)
        );
    }

    @Transactional
    public JobApplication create(String userId, ApplicationDtos.CreateRequest req) {
        JobApplication app = new JobApplication();
        app.setUserId(userId);
        app.setCompany(req.company());
        app.setRole(req.role());

        String status = (req.status() == null || req.status().isBlank()) ? "APPLIED" : req.status();
        app.setStatus(status);

        app.setJobUrl(req.jobUrl());
        app.setLocation(req.location());
        app.setSalaryRange(req.salaryRange());
        app.setAppliedDate(req.appliedDate());
        app.setDescription(req.description());
        app.setLastStatusAt(Instant.now());

        app = appRepo.save(app);

        StatusHistory h = new StatusHistory();
        h.setApplicationId(app.getId());
        h.setUserId(userId);
        h.setFromStatus("NEW");
        h.setToStatus(status);
        h.setNote("Created");
        h.setChangedAt(Instant.now());
        historyRepo.save(h);

        realtimePublisher.publishUserEvent(userId, "APPLICATION_CREATED", app);
        return app;
    }

    @Transactional
    public JobApplication update(String userId, String id, ApplicationDtos.UpdateRequest req) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        if (req.company() != null) app.setCompany(req.company());
        if (req.role() != null) app.setRole(req.role());
        app.setJobUrl(req.jobUrl());
        app.setLocation(req.location());
        app.setSalaryRange(req.salaryRange());
        app.setAppliedDate(req.appliedDate());
        app.setDescription(req.description());

        app = appRepo.save(app);
        realtimePublisher.publishUserEvent(userId, "APPLICATION_UPDATED", app);
        return app;
    }

    @Transactional
    public JobApplication changeStatus(String userId, String id, ApplicationDtos.StatusChangeRequest req) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        String from = app.getStatus();
        String to = req.toStatus();

        app.setStatus(to);
        app.setLastStatusAt(Instant.now());
        app = appRepo.save(app);

        StatusHistory h = new StatusHistory();
        h.setApplicationId(id);
        h.setUserId(userId);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setNote(req.note());
        h.setChangedAt(Instant.now());
        historyRepo.save(h);

        realtimePublisher.publishUserEvent(userId, "STATUS_CHANGED", app);
        return app;
    }

    @Transactional
    public void delete(String userId, String id) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        appRepo.delete(app);
        realtimePublisher.publishUserEvent(userId, "APPLICATION_DELETED", id);
    }

    @Transactional
    public Note addNote(String userId, String appId, ApplicationDtos.NoteCreateRequest req) {
        JobApplication app = appRepo.findById(appId).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        Note note = new Note();
        note.setUserId(userId);
        note.setApplicationId(appId);
        note.setBody(req.body());
        note = noteRepo.save(note);

        realtimePublisher.publishUserEvent(userId, "NOTE_ADDED", note);
        return note;
    }

    @Transactional
    public Reminder addReminder(String userId, String appId, ApplicationDtos.ReminderCreateRequest req) {
        JobApplication app = appRepo.findById(appId).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");

        Reminder r = new Reminder();
        r.setUserId(userId);
        r.setApplicationId(appId);
        r.setTitle(req.title());
        r.setDueAt(req.dueAt());
        r.setSent(false);
        r = reminderRepo.save(r);

        realtimePublisher.publishUserEvent(userId, "REMINDER_ADDED", r);
        return r;
    }
}
