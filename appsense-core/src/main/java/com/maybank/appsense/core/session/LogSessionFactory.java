package com.maybank.appsense.core.session;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.event.EventBus;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.ssh.HostKeyVerifierService;
import com.maybank.appsense.core.ssh.SshService;
import com.maybank.appsense.core.tail.RotationDetector;

public final class LogSessionFactory {

    private final ConfigService configService;

    public LogSessionFactory(ConfigService configService) {
        this.configService = configService;
    }

    /** Builds a session with its own event bus, SSH connection and state. */
    public LogSession create() {
        SettingsBean settings = configService.getSettings();
        EventBus eventBus = new EventBus();
        HostKeyVerifierService hostKeys = new HostKeyVerifierService(configService);
        SshService sshService = new SshService(settings, hostKeys);
        SessionStore store = new SessionStore(settings);
        SessionPipeline pipeline = new SessionPipeline(store, eventBus, settings);
        SessionViewControl viewControl = new SessionViewControl(store, eventBus, settings);
        SessionConnector connector = new SessionConnector(sshService, pipeline, eventBus, new RotationDetector());
        return new LogSession(configService, eventBus, hostKeys, sshService, store, pipeline, viewControl, connector);
    }
}
