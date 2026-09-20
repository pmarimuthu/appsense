package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class HostKeyChangedException extends AppSenseException {

    private final String newFingerprint;

    public HostKeyChangedException(String host, String storedFingerprint, String newFingerprint) {
        super(MessageKey.ERROR_HOST_KEY_CHANGED, null, host, storedFingerprint, newFingerprint);
        this.newFingerprint = newFingerprint;
    }

    public String getNewFingerprint() {
        return newFingerprint;
    }
}
