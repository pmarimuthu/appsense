package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class MaxReconnectException extends AppSenseException {

    public MaxReconnectException(String host, int attempts) {
        super(MessageKey.ERROR_RECONNECT_FAILED, null, host, attempts);
    }
}
