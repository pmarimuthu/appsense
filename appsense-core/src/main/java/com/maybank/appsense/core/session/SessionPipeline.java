package com.maybank.appsense.core.session;

import com.maybank.appsense.core.event.DeploymentDetectedEvent;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.LineAddedEvent;
import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.ServerType;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.core.parse.ParseService;
import com.maybank.appsense.core.parse.StackTraceAssembler;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/** Turns raw lines into stream items each batch tick and publishes what the faces should show. */
final class SessionPipeline {

    private final SessionStore store;
    private final EventBus eventBus;
    private final SettingsBean settings;
    private final Queue<StreamInput> inbox = new ConcurrentLinkedQueue<>();
    private StackTraceAssembler assembler;
    private ServerType serverType = ServerType.JBOSS;
    private volatile DeploymentMarkerRecord historicMarker;
    private PauseStateEvent lastPauseEvent;

    SessionPipeline(SessionStore store, EventBus eventBus, SettingsBean settings) {
        this.store = store;
        this.eventBus = eventBus;
        this.settings = settings;
    }

    void configure(ServerType type) {
        serverType = type;
        reset();
    }

    void reset() {
        ReentrantLock lock = store.lock();
        lock.lock();
        try {
            assembler = new StackTraceAssembler(new ParseService(serverType));
            inbox.clear();
            historicMarker = null;
            store.clear();
            eventBus.publish(new LineAddedEvent(List.of(), true));
            eventBus.publish(store.statsEvent());
        } finally {
            lock.unlock();
        }
    }

    void offerLine(String rawLine) {
        inbox.add(StreamInput.ofLine(rawLine));
    }

    void offerItem(StreamItem item) {
        inbox.add(StreamInput.ofItem(item));
    }

    void offerHistoricMarker(DeploymentMarkerRecord marker) {
        historicMarker = marker;
    }

    void tick() {
        ReentrantLock lock = store.lock();
        lock.lock();
        try {
            List<StreamItem> batch = new ArrayList<>();
            boolean refresh = drainInbox(batch);
            publish(batch, refresh);
        } finally {
            lock.unlock();
        }
    }

    private boolean drainInbox(List<StreamItem> batch) {
        boolean refresh = false;
        StreamInput input = inbox.poll();
        while (input != null) {
            if (input.isLine()) {
                for (LogLineRecord record : assembler.accept(input.rawLine())) {
                    refresh |= ingest(record, batch);
                }
            } else {
                refresh |= ingest(input.item(), batch);
            }
            input = inbox.poll();
        }
        for (LogLineRecord record : assembler.flushIdle()) {
            refresh |= ingest(record, batch);
        }
        return refresh;
    }

    private boolean ingest(StreamItem item, List<StreamItem> batch) {
        boolean refresh = releaseHistoricMarker(item, batch);
        store.append(item);
        boolean paused = store.isPaused();
        if (item instanceof DeploymentMarkerRecord marker) {
            return onMarker(marker, paused, batch) || refresh;
        }
        if (paused) {
            return refresh;
        }
        if (item instanceof LogLineRecord line) {
            store.count(line);
        }
        if (store.filters().accepts(item)) {
            batch.add(item);
        }
        return refresh;
    }

    private boolean releaseHistoricMarker(StreamItem item, List<StreamItem> batch) {
        DeploymentMarkerRecord marker = historicMarker;
        if (marker == null || !(item instanceof LogLineRecord) || item.timestamp().isBefore(marker.timestamp())) {
            return false;
        }
        historicMarker = null;
        return ingest(marker, batch);
    }

    private boolean onMarker(DeploymentMarkerRecord marker, boolean paused, List<StreamItem> batch) {
        eventBus.publish(new DeploymentDetectedEvent(marker));
        if (paused) {
            store.pauseBuffer().markDeployment(marker.timestamp());
            return false;
        }
        if (settings.isAutoDiscardOnDeployment()) {
            store.dropBeforeLastMarker();
            return true;
        }
        if (store.filters().isSinceLastDeployment()) {
            return true;
        }
        batch.add(marker);
        return false;
    }

    private void publish(List<StreamItem> batch, boolean refresh) {
        if (store.isPaused()) {
            publishPauseState();
        } else if (refresh) {
            store.rebuildStats();
            eventBus.publish(new LineAddedEvent(store.visible(), true));
            eventBus.publish(store.statsEvent());
        } else if (!batch.isEmpty()) {
            eventBus.publish(new LineAddedEvent(batch, false));
            eventBus.publish(store.statsEvent());
        }
    }

    private void publishPauseState() {
        PauseStateEvent event = store.pauseBuffer().toEvent(Instant.now());
        if (!event.equals(lastPauseEvent)) {
            lastPauseEvent = event;
            eventBus.publish(event);
        }
    }
}
