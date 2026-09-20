package com.maybank.appsense.core.model;

import java.time.Instant;

public record SummaryRowRecord(
        int count,
        String exceptionType,
        String message,
        String location,
        Instant firstSeen,
        Instant lastSeen
) {
}
