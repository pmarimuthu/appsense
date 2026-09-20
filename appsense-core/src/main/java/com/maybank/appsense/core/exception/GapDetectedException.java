package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class GapDetectedException extends AppSenseException {

    private final String path;

    public GapDetectedException(String path) {
        super(MessageKey.INFO_LOG_GAP, null);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
