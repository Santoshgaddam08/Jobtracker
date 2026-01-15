package com.santosh.jobtracker.applications;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository appRepo;
    private final StatusHistoryRepository historyRepo;

    public JobApplicationService(JobApplicationRepository appRepo, StatusHistoryRepository historyRepo) {
        this.appRepo = appRepo;
        this.historyRepo = historyRepo;
    }

    public List<JobApplication> list(String userId) {
        return appRepo.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public ApplicationDtos.DetailResponse detail(String userId, String id) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");
        return new ApplicationDtos.DetailResponse(
                app,
                historyRepo.findByUserIdAndApplicationIdOrderByChangedAtDesc(userId, id)
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

        return appRepo.save(app);
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

        return app;
    }

    @Transactional
    public void delete(String userId, String id) {
        JobApplication app = appRepo.findById(id).orElseThrow();
        if (!app.getUserId().equals(userId)) throw new IllegalArgumentException("Forbidden");
        appRepo.delete(app);
    }
}
