package com.maybank.appsense.core.model;

import java.util.List;
import java.util.Map;

public record StatsSnapshotRecord(
        int errorCount,
        int warningCount,
        List<Integer> errorsPerMinute,
        Map<String, Integer> topExceptions
) {
}
