package com.maybank.appsense.core.ssh;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.model.ProfileBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostKeyVerifierService {

    private static final Logger log = LoggerFactory.getLogger(HostKeyVerifierService.class);

    private final ConfigService configService;

    public HostKeyVerifierService(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Compares the presented fingerprint with the one stored on the profile. A first-time
     * fingerprint is stored immediately; a different one is reported and never stored here.
     */
    public HostKeyStatus evaluate(ProfileBean profile, String fingerprint) {
        String stored = profile.getHostKeyFingerprint();
        if (stored == null || stored.isBlank()) {
            store(profile, fingerprint);
            return HostKeyStatus.NEW;
        }
        return stored.equals(fingerprint) ? HostKeyStatus.TRUSTED : HostKeyStatus.CHANGED;
    }

    /** Stores a fingerprint the user has confirmed after a change warning. */
    public void trust(ProfileBean profile, String fingerprint) throws ConfigException {
        profile.setHostKeyFingerprint(fingerprint);
        configService.saveProfile(profile);
    }

    private void store(ProfileBean profile, String fingerprint) {
        profile.setHostKeyFingerprint(fingerprint);
        try {
            configService.saveProfile(profile);
        } catch (ConfigException ex) {
            log.error("Host key fingerprint for {} could not be saved", profile.getName(), ex);
        }
    }
}
