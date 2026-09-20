package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class AppSenseException extends Exception {

    private final MessageKey messageKey;
    private final Object[] args;

    public AppSenseException(MessageKey messageKey, Throwable cause, Object... args) {
        super(messageKey.getKey(), cause);
        this.messageKey = messageKey;
        this.args = args.clone();
    }

    public MessageKey getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args.clone();
    }
}
