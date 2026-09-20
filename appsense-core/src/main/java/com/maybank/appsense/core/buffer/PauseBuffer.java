package com.maybank.appsense.core.buffer;

import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.core.util.DurationFormatUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Holds items that arrive while the view is paused. Not thread-safe; the session lock guards it. */
public final class PauseBuffer {

    private final RingBuffer<StreamItem> items;
    private boolean paused;
    private Instant pausedAt = Instant.EPOCH;
    private long newLines;
    private boolean overflowed;
    private Instant deploymentDetectedAt;

    public PauseBuffer(int capacity) {
        this.items = new RingBuffer<>(capacity);
    }

    public void start(Instant now) {
        items.clear();
        paused = true;
        pausedAt = now;
        newLines = 0;
        overflowed = false;
        deploymentDetectedAt = null;
    }

    public void add(StreamItem item) {
        if (items.add(item)) {
            overflowed = true;
        }
        if (item instanceof LogLineRecord) {
            newLines++;
        }
    }

    public void markDeployment(Instant when) {
        deploymentDetectedAt = when;
    }

    /** Drops collected items that precede the latest deployment marker. */
    public void discardBeforeLastMarker() {
        int marker = items.lastIndexOf(item -> item instanceof DeploymentMarkerRecord);
        if (marker > 0) {
            items.dropFirst(marker);
        }
        newLines = items.toList().stream().filter(item -> item instanceof LogLineRecord).count();
    }

    /** Ends the pause and returns what was collected, oldest first. */
    public List<StreamItem> drain() {
        List<StreamItem> collected = items.toList();
        items.clear();
        paused = false;
        return collected;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean hasOverflowed() {
        return overflowed;
    }

    public PauseStateEvent toEvent(Instant now) {
        String elapsed = DurationFormatUtil.formatDdHhMm(Duration.between(pausedAt, now));
        return new PauseStateEvent(paused, elapsed, newLines, overflowed, deploymentDetectedAt);
    }
}
