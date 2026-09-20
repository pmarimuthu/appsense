package com.maybank.appsense.core.session;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.exception.LogPathException;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.ssh.HostKeyVerifierService;
import com.maybank.appsense.core.ssh.SshService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Single entry point a face uses for one live log connection. Create through LogSessionFactory. */
public final class LogSession {

    private static final Logger log = LoggerFactory.getLogger(LogSession.class);

    private final ConfigService configService;
    private final EventBus eventBus;
    private final HostKeyVerifierService hostKeyVerifierService;
    private final SshService sshService;
    private final SessionStore store;
    private final SessionPipeline pipeline;
    private final SessionViewControl viewControl;
    private final SessionConnector connector;
    private ScheduledExecutorService batchTimer;

    LogSession(ConfigService configService, EventBus eventBus, HostKeyVerifierService hostKeyVerifierService,
               SshService sshService, SessionStore store, SessionPipeline pipeline,
               SessionViewControl viewControl, SessionConnector connector) {
        this.configService = configService;
        this.eventBus = eventBus;
        this.hostKeyVerifierService = hostKeyVerifierService;
        this.sshService = sshService;
        this.store = store;
        this.pipeline = pipeline;
        this.viewControl = viewControl;
        this.connector = connector;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    /** Connects and follows the last used log of the profile, or its first log. */
    public void connect(ProfileBean profile, char[] password) throws AppSenseException {
        connect(profile, password, chooseLog(profile));
    }

    /** Opens SSH, starts the tail and the deployment watch. The password is copied and wiped on disconnect. */
    public void connect(ProfileBean profile, char[] password, LogEntryBean logEntry) throws AppSenseException {
        store.configure(profile.getAppPackages());
        connector.connect(profile, password, logEntry);
        startBatchTimer();
        rememberSelection(profile, logEntry);
    }

    /** Closes SSH and wipes the in-memory password. */
    public void disconnect() {
        stopBatchTimer();
        connector.disconnect();
    }

    public void switchLog(LogEntryBean logEntry) throws AppSenseException {
        connector.switchLog(logEntry);
        rememberSelection(null, logEntry);
    }

    public void pause() {
        viewControl.pause();
    }

    public void resume() {
        viewControl.resume();
    }

    public void setLevelFilter(LevelFilter filter) {
        viewControl.setLevelFilter(filter);
    }

    public void setKeywordFilter(String keyword) {
        viewControl.setKeywordFilter(keyword);
    }

    public void setSinceLastDeployment(boolean enabled) {
        viewControl.setSinceLastDeployment(enabled);
    }

    public void discardPrevious() {
        viewControl.discardPrevious();
    }

    public void markDeployment() {
        connector.markDeployment();
        pipeline.tick();
    }

    /** Stores a changed host key fingerprint the user has confirmed; call connect again afterwards. */
    public void trustHostKey(ProfileBean profile, String fingerprint) throws ConfigException {
        hostKeyVerifierService.trust(profile, fingerprint);
    }

    public boolean isConnected() {
        return sshService.isConnected();
    }

    public boolean isPaused() {
        store.lock().lock();
        try {
            return store.isPaused();
        } finally {
            store.lock().unlock();
        }
    }

    public void close() {
        disconnect();
        eventBus.close();
    }

    private LogEntryBean chooseLog(ProfileBean profile) throws LogPathException {
        String lastUsed = configService.getSettings().getLastUsedLog();
        return profile.findLog(lastUsed).or(() -> profile.getLogs().stream().findFirst())
                .orElseThrow(() -> new LogPathException(profile.getName()));
    }

    private void rememberSelection(ProfileBean profile, LogEntryBean logEntry) {
        SettingsBean settings = configService.getSettings();
        if (profile != null) {
            settings.setLastUsedProfile(profile.getName());
        }
        settings.setLastUsedLog(logEntry.getLabel());
        try {
            configService.saveSettings();
        } catch (ConfigException ex) {
            log.error("Last used selection could not be saved", ex);
            eventBus.publish(NoticeEvent.error(ex));
        }
    }

    private void startBatchTimer() {
        stopBatchTimer();
        batchTimer = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().daemon(true).name(AppConstant.BATCH_THREAD).factory());
        long interval = configService.getSettings().getUiBatchIntervalMs();
        batchTimer.scheduleWithFixedDelay(this::safeTick, interval, interval, TimeUnit.MILLISECONDS);
    }

    private void stopBatchTimer() {
        if (batchTimer != null) {
            batchTimer.shutdownNow();
            batchTimer = null;
        }
    }

    private void safeTick() {
        try {
            pipeline.tick();
        } catch (RuntimeException ex) {
            log.error("Batch tick failed", ex);
        }
    }
}
