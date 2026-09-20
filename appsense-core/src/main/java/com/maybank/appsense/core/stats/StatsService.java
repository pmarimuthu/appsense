package com.maybank.appsense.core.stats;

import com.maybank.appsense.core.config.FilterConstant;
import com.maybank.appsense.core.model.ExceptionInfoRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StatsSnapshotRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Running error and warning counts, a per-minute error timeline and the top exception types. */
public final class StatsService {

    private final Map<Long, Integer> errorsByMinute = new TreeMap<>();
    private final Map<String, Integer> exceptionCounts = new HashMap<>();
    private int errorCount;
    private int warningCount;
    private long latestMinute;
    private boolean hasData;

    /** Counts one entry. The exception info is only used, and required, for error-level entries. */
    public void accept(LogLineRecord line, ExceptionInfoRecord info) {
        long minute = line.timestamp().getEpochSecond() / FilterConstant.SECONDS_PER_MINUTE;
        if (!hasData || minute > latestMinute) {
            latestMinute = minute;
            hasData = true;
        }
        if (line.level().isError()) {
            errorCount++;
            errorsByMinute.merge(minute, 1, Integer::sum);
            exceptionCounts.merge(info.type(), 1, Integer::sum);
        } else if (line.level().isWarning()) {
            warningCount++;
        }
        prune();
    }

    public void reset() {
        errorsByMinute.clear();
        exceptionCounts.clear();
        errorCount = 0;
        warningCount = 0;
        latestMinute = 0;
        hasData = false;
    }

    public StatsSnapshotRecord snapshot() {
        return new StatsSnapshotRecord(errorCount, warningCount, timeline(), topExceptions());
    }

    private List<Integer> timeline() {
        List<Integer> window = new ArrayList<>(FilterConstant.STATS_WINDOW_MINUTES);
        long first = latestMinute - (FilterConstant.STATS_WINDOW_MINUTES - 1);
        for (int slot = 0; slot < FilterConstant.STATS_WINDOW_MINUTES; slot++) {
            window.add(errorsByMinute.getOrDefault(first + slot, 0));
        }
        return window;
    }

    private Map<String, Integer> topExceptions() {
        Map<String, Integer> top = new LinkedHashMap<>();
        exceptionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(FilterConstant.TOP_EXCEPTIONS_LIMIT)
                .forEach(entry -> top.put(entry.getKey(), entry.getValue()));
        return top;
    }

    private void prune() {
        long oldestKept = latestMinute - (FilterConstant.STATS_WINDOW_MINUTES - 1);
        errorsByMinute.keySet().removeIf(minute -> minute < oldestKept);
    }
}
