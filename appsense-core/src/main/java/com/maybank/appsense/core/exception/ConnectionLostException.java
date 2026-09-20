package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class ConnectionLostException extends AppSenseException {

    public ConnectionLostException(String host, Throwable cause) {
        super(MessageKey.ERROR_CONNECTION_LOST, cause, host);
    }
}
