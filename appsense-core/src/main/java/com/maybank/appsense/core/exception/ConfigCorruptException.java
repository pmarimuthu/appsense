package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class ConfigCorruptException extends ConfigException {

    public ConfigCorruptException(String path, Throwable cause) {
        super(MessageKey.ERROR_CONFIG_CORRUPT, cause, path);
    }
}
