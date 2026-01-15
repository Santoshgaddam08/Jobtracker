package com.santosh.jobtracker.analytics;

import java.util.List;
import java.util.Map;

public class AnalyticsDtos {

    public record SummaryResponse(
            long totalApplications,
            Map<String, Long> byStatus,
            long createdLast7Days,
            long statusChangesLast7Days,
            List<CompanyCount> topCompanies,
            double avgDaysSinceApplied
    ) {}

    public record CompanyCount(String company, long count) {}
}
