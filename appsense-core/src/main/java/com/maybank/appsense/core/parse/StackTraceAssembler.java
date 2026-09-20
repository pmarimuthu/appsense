package com.maybank.appsense.core.parse;

import com.maybank.appsense.core.config.ParseConstant;
import com.maybank.appsense.core.model.LogLevel;
import com.maybank.appsense.core.model.LogLineRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StackTraceAssembler {

    private final ParseService parseService;
    private final List<String> pendingLines = new ArrayList<>();
    private LogLineRecord pendingHeader;
    private Instant lastTimestamp = Instant.EPOCH;
    private boolean touchedSinceFlush;

    public StackTraceAssembler(ParseService parseService) {
        this.parseService = parseService;
    }

    /** Feeds one raw line and returns any entries that are now complete. */
    public List<LogLineRecord> accept(String rawLine) {
        List<LogLineRecord> completed = new ArrayList<>();
        Optional<LogLineRecord> header = parseService.parseHeader(rawLine);
        if (header.isPresent()) {
            finishPending(completed);
            pendingHeader = header.get();
            lastTimestamp = pendingHeader.timestamp();
        } else if (pendingHeader != null) {
            appendContinuation(rawLine);
        } else if (!rawLine.isBlank()) {
            completed.add(new LogLineRecord(lastTimestamp, LogLevel.OTHER, "", rawLine, List.of()));
        }
        touchedSinceFlush = true;
        return completed;
    }

    /** Completes the pending entry when no line arrived since the previous call. */
    public List<LogLineRecord> flushIdle() {
        List<LogLineRecord> completed = new ArrayList<>();
        if (!touchedSinceFlush) {
            finishPending(completed);
        }
        touchedSinceFlush = false;
        return completed;
    }

    public void reset() {
        pendingHeader = null;
        pendingLines.clear();
        lastTimestamp = Instant.EPOCH;
        touchedSinceFlush = false;
    }

    private void appendContinuation(String rawLine) {
        if (pendingLines.size() < ParseConstant.MAX_STACK_LINES) {
            pendingLines.add(rawLine);
        }
    }

    private void finishPending(List<LogLineRecord> completed) {
        if (pendingHeader == null) {
            return;
        }
        completed.add(new LogLineRecord(
                pendingHeader.timestamp(),
                pendingHeader.level(),
                pendingHeader.logger(),
                pendingHeader.message(),
                List.copyOf(pendingLines)));
        pendingHeader = null;
        pendingLines.clear();
    }
}
