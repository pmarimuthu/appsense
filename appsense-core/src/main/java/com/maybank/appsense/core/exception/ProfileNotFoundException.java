package com.maybank.appsense.core.exception;

import com.maybank.appsense.core.i18n.MessageKey;

public class ProfileNotFoundException extends ConfigException {

    public ProfileNotFoundException(String profileName) {
        super(MessageKey.ERROR_PROFILE_NOT_FOUND, null, profileName);
    }
}
