package com.santosh.jobtracker.analytics;

import com.santosh.jobtracker.applications.JobApplication;
import com.santosh.jobtracker.applications.JobApplicationRepository;
import com.santosh.jobtracker.applications.StatusHistory;
import com.santosh.jobtracker.applications.StatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final JobApplicationRepository appRepo;
    private final StatusHistoryRepository historyRepo;

    public AnalyticsService(JobApplicationRepository appRepo, StatusHistoryRepository historyRepo) {
        this.appRepo = appRepo;
        this.historyRepo = historyRepo;
    }

    public AnalyticsDtos.SummaryResponse summary(String userId) {
        List<JobApplication> apps = appRepo.findByUserIdOrderByUpdatedAtDesc(userId);
        long total = apps.size();

        Map<String, Long> byStatus = apps.stream()
                .collect(Collectors.groupingBy(
                        a -> (a.getStatus() == null ? "UNKNOWN" : a.getStatus()),
                        Collectors.counting()
                ));

        Instant now = Instant.now();
        Instant last7 = now.minus(7, ChronoUnit.DAYS);

        long createdLast7Days = apps.stream()
                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(last7))
                .count();

        // We don’t have a "findByUserId" method for status history yet, so we compute from all history items
        // but filtered by the user in memory using existing query per application.
        long statusChangesLast7Days = 0;
        for (JobApplication a : apps) {
            List<StatusHistory> h = historyRepo.findByUserIdAndApplicationIdOrderByChangedAtDesc(userId, a.getId());
            statusChangesLast7Days += h.stream()
                    .filter(x -> x.getChangedAt() != null && x.getChangedAt().isAfter(last7))
                    .count();
        }

        // Top companies
        Map<String, Long> companyCounts = apps.stream()
                .filter(a -> a.getCompany() != null && !a.getCompany().isBlank())
                .collect(Collectors.groupingBy(JobApplication::getCompany, Collectors.counting()));

        List<AnalyticsDtos.CompanyCount> topCompanies = companyCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> new AnalyticsDtos.CompanyCount(e.getKey(), e.getValue()))
                .toList();

        // Avg days since applied (if appliedDate exists)
        List<Long> days = apps.stream()
                .filter(a -> a.getAppliedDate() != null)
                .map(a -> ChronoUnit.DAYS.between(a.getAppliedDate(), now))
                .filter(d -> d >= 0)
                .toList();

        double avgDaysSinceApplied = days.isEmpty()
                ? 0.0
                : days.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new AnalyticsDtos.SummaryResponse(
                total,
                byStatus,
                createdLast7Days,
                statusChangesLast7Days,
                topCompanies,
                avgDaysSinceApplied
        );
    }
}
