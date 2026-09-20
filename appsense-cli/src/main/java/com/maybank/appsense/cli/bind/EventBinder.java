package com.maybank.appsense.cli.bind;

import com.maybank.appsense.cli.screen.FullScreenRenderer;
import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.DeploymentDetectedEvent;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.GapMarkerEvent;
import com.maybank.appsense.core.event.LineAddedEvent;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.event.StatsUpdatedEvent;
import java.util.List;

/**
 * Subscribes all core events to the {@link FullScreenRenderer} redraw callbacks.
 * Call {@link #bind(EventBus)} on each new session.
 */
public final class EventBinder {

    private final FullScreenRenderer renderer;

    public EventBinder(FullScreenRenderer renderer) {
        this.renderer = renderer;
    }

    /** Subscribes to every event the renderer needs. There is no unsubscribe. */
    public void bind(EventBus bus) {
        bus.subscribe(LineAddedEvent.class, event -> {
            if (event.reset()) {
                renderer.resetLines(List.copyOf(event.items()));
            } else {
                renderer.onLines(List.copyOf(event.items()));
            }
        });
        bus.subscribe(PauseStateEvent.class, renderer::onPauseState);
        bus.subscribe(ConnectionStateEvent.class, renderer::onConnectionState);
        bus.subscribe(StatsUpdatedEvent.class, event ->
                renderer.onStats(event.stats(), event.summary()));
        bus.subscribe(DeploymentDetectedEvent.class, event ->
                renderer.onLines(List.of(event.marker())));
        bus.subscribe(GapMarkerEvent.class, event ->
                renderer.onLines(List.of(event.marker())));
        bus.subscribe(NoticeEvent.class, event -> renderer.redraw());
    }
}
