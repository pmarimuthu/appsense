package com.maybank.appsense.core.session;

import com.maybank.appsense.core.buffer.PauseBuffer;
import com.maybank.appsense.core.buffer.RingBuffer;
import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.FilterConstant;
import com.maybank.appsense.core.event.StatsUpdatedEvent;
import com.maybank.appsense.core.filter.FilterService;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.ExceptionInfoRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.core.stats.StatsService;
import com.maybank.appsense.core.summary.ExceptionExtractUtil;
import com.maybank.appsense.core.summary.SummaryService;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/** All mutable session state. Every method except lock() must be called while holding the lock. */
final class SessionStore {

    private final RingBuffer<StreamItem> history = new RingBuffer<>(FilterConstant.HISTORY_CAPACITY);
    private final FilterService filterService = new FilterService();
    private final StatsService statsService = new StatsService();
    private final SummaryService summaryService = new SummaryService();
    private final PauseBuffer pauseBuffer;
    private final ReentrantLock lock = new ReentrantLock();
    private List<String> appPackages = List.of();
    private int appendedSincePause;

    SessionStore(SettingsBean settings) {
        this.pauseBuffer = new PauseBuffer(settings.getPauseBufferCap());
    }

    ReentrantLock lock() {
        return lock;
    }

    FilterService filters() {
        return filterService;
    }

    PauseBuffer pauseBuffer() {
        return pauseBuffer;
    }

    boolean isPaused() {
        return pauseBuffer.isPaused();
    }

    void configure(List<String> packages) {
        appPackages = List.copyOf(packages);
    }

    void append(StreamItem item) {
        history.add(item);
        if (isPaused()) {
            appendedSincePause++;
            pauseBuffer.add(item);
        }
    }

    void count(LogLineRecord line) {
        ExceptionInfoRecord info = line.level().isError() ? ExceptionExtractUtil.extract(line, appPackages) : null;
        statsService.accept(line, info);
        if (info != null) {
            summaryService.accept(line, info);
        }
    }

    void startPause(Instant now) {
        appendedSincePause = 0;
        pauseBuffer.start(now);
    }

    List<StreamItem> endPause() {
        List<StreamItem> pending = pauseBuffer.drain();
        appendedSincePause = 0;
        return pending;
    }

    List<StreamItem> visible() {
        int limit = viewLimit();
        return filterService.apply(history, filterService.scopeStart(history, limit), limit);
    }

    void rebuildStats() {
        statsService.reset();
        summaryService.reset();
        int limit = viewLimit();
        for (int index = filterService.scopeStart(history, limit); index < limit; index++) {
            if (history.get(index) instanceof LogLineRecord line) {
                count(line);
            }
        }
    }

    StatsUpdatedEvent statsEvent() {
        return new StatsUpdatedEvent(statsService.snapshot(), summaryService.snapshot());
    }

    void dropBeforeLastMarker() {
        int marker = history.lastIndexOf(item -> item instanceof DeploymentMarkerRecord);
        if (marker > 0) {
            history.dropFirst(marker);
        }
    }

    void clear() {
        history.clear();
        statsService.reset();
        summaryService.reset();
        appendedSincePause = 0;
    }

    private int viewLimit() {
        return isPaused() ? Math.max(history.size() - appendedSincePause, 0) : history.size();
    }
}
