package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class LogPathException extends AppSenseException {

    public LogPathException(String path) {
        super(MessageKey.ERROR_LOG_NOT_FOUND, null, path);
    }

    public LogPathException(String path, Throwable cause) {
        super(MessageKey.ERROR_LOG_NOT_FOUND, cause, path);
    }
}
