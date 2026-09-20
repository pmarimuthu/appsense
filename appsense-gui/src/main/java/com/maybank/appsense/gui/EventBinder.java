package com.maybank.appsense.gui;

import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.DeploymentDetectedEvent;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.GapMarkerEvent;
import com.maybank.appsense.core.event.LineAddedEvent;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.event.StatsUpdatedEvent;
import com.maybank.appsense.gui.panel.FilterBar;
import com.maybank.appsense.gui.panel.LogPanel;
import com.maybank.appsense.gui.panel.PauseBar;
import com.maybank.appsense.gui.panel.StatsPanel;
import com.maybank.appsense.gui.panel.StatusBar;
import com.maybank.appsense.gui.panel.SummaryPanel;
import com.maybank.appsense.gui.panel.TopBar;

/**
 * Wires all GUI panels to a fresh {@link EventBus}.
 * Call {@link #bind(EventBus)} each time a new {@code LogSession} is created.
 */
public final class EventBinder {

    private final TopBar topBar;
    private final PauseBar pauseBar;
    private final FilterBar filterBar;
    private final StatusBar statusBar;
    private final LogPanel logPanel;
    private final StatsPanel statsPanel;
    private final SummaryPanel summaryPanel;

    public EventBinder(TopBar topBar,
                       PauseBar pauseBar,
                       FilterBar filterBar,
                       StatusBar statusBar,
                       LogPanel logPanel,
                       StatsPanel statsPanel,
                       SummaryPanel summaryPanel) {
        this.topBar = topBar;
        this.pauseBar = pauseBar;
        this.filterBar = filterBar;
        this.statusBar = statusBar;
        this.logPanel = logPanel;
        this.statsPanel = statsPanel;
        this.summaryPanel = summaryPanel;
    }

    /** Subscribes all panels to the given bus. No-op if bus is null. */
    public void bind(EventBus bus) {
        if (bus == null) {
            return;
        }
        bus.subscribe(LineAddedEvent.class, logPanel::onLineAdded);
        bus.subscribe(PauseStateEvent.class, pauseBar::onPauseState);
        bus.subscribe(PauseStateEvent.class, topBar::onPauseState);
        bus.subscribe(ConnectionStateEvent.class, topBar::onConnectionState);
        bus.subscribe(ConnectionStateEvent.class, statusBar::onConnectionState);
        bus.subscribe(StatsUpdatedEvent.class, statsPanel::onStats);
        bus.subscribe(StatsUpdatedEvent.class, summaryPanel::onStats);
        bus.subscribe(DeploymentDetectedEvent.class, statusBar::onDeployment);
        bus.subscribe(GapMarkerEvent.class, logPanel::onGapMarker);
        bus.subscribe(NoticeEvent.class, statusBar::onNotice);
    }
}
