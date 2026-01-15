package com.santosh.jobtracker.applications;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    private String userId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list(userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable String id) {
        return ResponseEntity.ok(service.detail(userId(), id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationDtos.CreateRequest req) {
        return ResponseEntity.ok(service.create(userId(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody ApplicationDtos.UpdateRequest req) {
        return ResponseEntity.ok(service.update(userId(), id, req));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable String id, @RequestBody ApplicationDtos.StatusChangeRequest req) {
        return ResponseEntity.ok(service.changeStatus(userId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.delete(userId(), id);
        return ResponseEntity.noContent().build();
    }
@PostMapping("/{id}/notes")
public ResponseEntity<?> addNote(@PathVariable String id, @RequestBody ApplicationDtos.NoteCreateRequest req) {
    return ResponseEntity.ok(service.addNote(userId(), id, req));
}

@PostMapping("/{id}/reminders")
public ResponseEntity<?> addReminder(@PathVariable String id, @RequestBody ApplicationDtos.ReminderCreateRequest req) {
    return ResponseEntity.ok(service.addReminder(userId(), id, req));
}
}
