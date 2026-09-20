package com.maybank.appsense.core.event;

import com.maybank.appsense.core.config.AppConstant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private final Map<Class<? extends AppEvent>, List<Consumer<AppEvent>>> listeners = new ConcurrentHashMap<>();
    private final BlockingQueue<AppEvent> queue = new LinkedBlockingQueue<>();
    private final Thread dispatcher;
    private volatile boolean running = true;

    public EventBus() {
        dispatcher = Thread.ofVirtual().name(AppConstant.EVENT_BUS_THREAD).start(this::dispatchLoop);
    }

    /** Queues an event. Delivery happens in order on the bus thread, never on the caller. */
    public void publish(AppEvent event) {
        if (running) {
            queue.add(event);
        }
    }

    public <T extends AppEvent> void subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>())
                .add(event -> listener.accept(type.cast(event)));
    }

    public void close() {
        running = false;
        dispatcher.interrupt();
    }

    private void dispatchLoop() {
        while (running) {
            try {
                deliver(queue.take());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void deliver(AppEvent event) {
        for (Consumer<AppEvent> listener : listeners.getOrDefault(event.getClass(), List.of())) {
            try {
                listener.accept(event);
            } catch (RuntimeException ex) {
                log.error("Listener failed for event {}", event.getClass().getSimpleName(), ex);
            }
        }
    }
}
