package com.maybank.appsense.core.model;

import java.time.Instant;
import java.util.List;

public record LogLineRecord(
        Instant timestamp,
        LogLevel level,
        String logger,
        String message,
        List<String> stackTrace
) implements StreamItem {

    public boolean hasStackTrace() {
        return !stackTrace.isEmpty();
    }
}
