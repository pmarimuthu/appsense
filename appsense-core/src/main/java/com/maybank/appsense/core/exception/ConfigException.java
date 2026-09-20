package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class ConfigException extends AppSenseException {

    public ConfigException(MessageKey messageKey, Throwable cause, Object... args) {
        super(messageKey, cause, args);
    }
}
