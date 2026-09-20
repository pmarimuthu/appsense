package com.maybank.appsense.core.summary;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.FilterConstant;
import com.maybank.appsense.core.model.ExceptionInfoRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.SummaryRowRecord;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Groups identical errors by exception type, message and first application frame. */
public final class SummaryService {

    private final Map<String, Group> groups = new HashMap<>();

    public void accept(LogLineRecord line, ExceptionInfoRecord info) {
        String key = info.type() + AppConstant.SUMMARY_KEY_SEPARATOR
                + info.message() + AppConstant.SUMMARY_KEY_SEPARATOR + info.location();
        groups.computeIfAbsent(key, ignored -> new Group(info, line.timestamp())).record(line.timestamp());
    }

    public void reset() {
        groups.clear();
    }

    public List<SummaryRowRecord> snapshot() {
        return groups.values().stream()
                .sorted(Comparator.comparingInt((Group group) -> group.count).reversed()
                        .thenComparing(group -> group.lastSeen, Comparator.reverseOrder()))
                .limit(FilterConstant.SUMMARY_MAX_ROWS)
                .map(Group::toRow)
                .toList();
    }

    private static final class Group {

        private final ExceptionInfoRecord info;
        private final Instant firstSeen;
        private Instant lastSeen;
        private int count;

        private Group(ExceptionInfoRecord info, Instant firstSeen) {
            this.info = info;
            this.firstSeen = firstSeen;
            this.lastSeen = firstSeen;
        }

        private void record(Instant timestamp) {
            count++;
            if (timestamp.isAfter(lastSeen)) {
                lastSeen = timestamp;
            }
        }

        private SummaryRowRecord toRow() {
            return new SummaryRowRecord(count, info.type(), info.message(), info.location(), firstSeen, lastSeen);
        }
    }
}
