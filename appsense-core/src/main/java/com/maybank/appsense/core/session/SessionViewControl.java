package com.maybank.appsense.core.session;

import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.LineAddedEvent;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.model.StreamItem;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/** View-changing operations: filters, scope, discard, pause and resume. */
final class SessionViewControl {

    private final SessionStore store;
    private final EventBus eventBus;
    private final SettingsBean settings;

    SessionViewControl(SessionStore store, EventBus eventBus, SettingsBean settings) {
        this.store = store;
        this.eventBus = eventBus;
        this.settings = settings;
    }

    void setLevelFilter(LevelFilter filter) {
        locked(() -> {
            store.filters().setLevelFilter(filter);
            refresh();
        });
    }

    void setKeywordFilter(String keyword) {
        locked(() -> {
            store.filters().setKeyword(keyword);
            refresh();
        });
    }

    void setSinceLastDeployment(boolean enabled) {
        locked(() -> {
            store.filters().setSinceLastDeployment(enabled);
            refresh();
        });
    }

    void discardPrevious() {
        locked(() -> {
            if (store.isPaused()) {
                store.pauseBuffer().discardBeforeLastMarker();
            }
            store.dropBeforeLastMarker();
            refresh();
        });
    }

    void pause() {
        locked(() -> {
            if (!store.isPaused()) {
                store.startPause(Instant.now());
                eventBus.publish(store.pauseBuffer().toEvent(Instant.now()));
            }
        });
    }

    void resume() {
        locked(() -> {
            if (store.isPaused()) {
                boolean overflowed = store.pauseBuffer().hasOverflowed();
                List<StreamItem> pending = store.endPause();
                eventBus.publish(store.pauseBuffer().toEvent(Instant.now()));
                flushPending(pending, overflowed);
            }
        });
    }

    private void flushPending(List<StreamItem> pending, boolean overflowed) {
        boolean deployed = pending.stream().anyMatch(item -> item instanceof DeploymentMarkerRecord);
        if (deployed && settings.isAutoDiscardOnDeployment()) {
            store.dropBeforeLastMarker();
        }
        if (overflowed || (deployed && (settings.isAutoDiscardOnDeployment() || store.filters().isSinceLastDeployment()))) {
            refresh();
            return;
        }
        List<StreamItem> accepted = pending.stream().filter(store.filters()::accepts).toList();
        store.rebuildStats();
        eventBus.publish(new LineAddedEvent(accepted, false));
        eventBus.publish(store.statsEvent());
    }

    private void refresh() {
        store.rebuildStats();
        eventBus.publish(new LineAddedEvent(store.visible(), true));
        eventBus.publish(store.statsEvent());
    }

    private void locked(Runnable action) {
        ReentrantLock lock = store.lock();
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}
