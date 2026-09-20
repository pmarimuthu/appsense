package com.maybank.appsense.core.session;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.SshConstant;
import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.AuthFailedException;
import com.maybank.appsense.core.exception.HostKeyChangedException;
import com.maybank.appsense.core.exception.MaxReconnectException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.ssh.SshService;
import com.maybank.appsense.core.tail.RotationDetector;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns the SSH connection lifecycle: connect, disconnect and the reconnect loop with backoff. */
final class SessionConnector {

    private static final Logger log = LoggerFactory.getLogger(SessionConnector.class);

    private final SshService sshService;
    private final EventBus eventBus;
    private final StreamController streams;
    private final AtomicBoolean recovering = new AtomicBoolean();
    private ProfileBean profile;
    private char[] password = new char[0];
    private volatile boolean closed = true;

    SessionConnector(SshService sshService, SessionPipeline pipeline, EventBus eventBus,
                     RotationDetector rotationDetector) {
        this.sshService = sshService;
        this.eventBus = eventBus;
        this.streams = new StreamController(sshService, pipeline, eventBus, rotationDetector, this::recover);
    }

    void connect(ProfileBean target, char[] secret, LogEntryBean logEntry) throws AppSenseException {
        profile = target;
        password = secret.clone();
        closed = false;
        publishState(ConnectionStateEvent.State.CONNECTING);
        try {
            sshService.connect(profile, password);
            streams.start(profile, logEntry);
        } catch (AppSenseException ex) {
            disconnect();
            throw ex;
        }
        publishState(ConnectionStateEvent.State.CONNECTED);
    }

    void disconnect() {
        closed = true;
        streams.stop();
        sshService.close();
        Arrays.fill(password, AppConstant.NUL_CHAR);
        if (profile != null) {
            publishState(ConnectionStateEvent.State.DISCONNECTED);
        }
    }

    void switchLog(LogEntryBean next) throws AppSenseException {
        streams.switchLog(next);
    }

    void markDeployment() {
        streams.markDeployment();
    }

    private void recover() {
        if (closed || !recovering.compareAndSet(false, true)) {
            return;
        }
        try {
            publishState(ConnectionStateEvent.State.RECONNECTING);
            eventBus.publish(new NoticeEvent(MessageKey.ERROR_CONNECTION_LOST,
                    List.of(sshService.getHost()), NoticeEvent.Severity.WARNING));
            streams.stop();
            reconnectLoop();
        } finally {
            recovering.set(false);
        }
    }

    private void reconnectLoop() {
        long delay = SshConstant.RECONNECT_INITIAL_DELAY_MS;
        for (int attempt = 1; attempt <= SshConstant.MAX_RECONNECT_ATTEMPTS && !closed; attempt++) {
            if (!waitFor(delay)) {
                return;
            }
            try {
                attemptReconnect();
                return;
            } catch (AuthFailedException | HostKeyChangedException ex) {
                giveUp(ex);
                return;
            } catch (AppSenseException ex) {
                log.warn("Reconnect attempt {} to {} failed", attempt, sshService.getHost(), ex);
                delay = Math.min(delay * SshConstant.RECONNECT_BACKOFF_FACTOR, SshConstant.RECONNECT_MAX_DELAY_MS);
            }
        }
        if (!closed) {
            giveUp(new MaxReconnectException(sshService.getHost(), SshConstant.MAX_RECONNECT_ATTEMPTS));
        }
    }

    private void attemptReconnect() throws AppSenseException {
        sshService.connect(profile, password);
        streams.resume();
        publishState(ConnectionStateEvent.State.CONNECTED);
        eventBus.publish(NoticeEvent.info(MessageKey.INFO_RECONNECTED, sshService.getHost()));
    }

    private void giveUp(AppSenseException reason) {
        eventBus.publish(NoticeEvent.error(reason));
        sshService.close();
        publishState(ConnectionStateEvent.State.DISCONNECTED);
    }

    private boolean waitFor(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void publishState(ConnectionStateEvent.State state) {
        eventBus.publish(new ConnectionStateEvent(state, profile.getName()));
    }
}
