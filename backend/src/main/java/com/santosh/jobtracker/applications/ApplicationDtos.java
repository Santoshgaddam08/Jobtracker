package com.santosh.jobtracker.applications;

import java.time.Instant;
import java.util.List;

public class ApplicationDtos {

    public record CreateRequest(
            String company,
            String role,
            String status,
            String jobUrl,
            String location,
            String salaryRange,
            Instant appliedDate,
            String description
    ) {}

    public record UpdateRequest(
            String company,
            String role,
            String jobUrl,
            String location,
            String salaryRange,
            Instant appliedDate,
            String description
    ) {}

    public record StatusChangeRequest(
            String toStatus,
            String note
    ) {}

    public record DetailResponse(
            JobApplication application,
            List<StatusHistory> history
    ) {}
}
