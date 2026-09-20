package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class AuthFailedException extends AppSenseException {

    public AuthFailedException(String host, Throwable cause) {
        super(MessageKey.ERROR_AUTH_FAILED, cause, host);
    }
}
