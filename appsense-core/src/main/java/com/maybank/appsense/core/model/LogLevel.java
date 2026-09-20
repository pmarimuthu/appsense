package com.maybank.appsense.core.model;

import com.maybank.appsense.core.config.ParseConstant;
import java.util.Locale;

public enum LogLevel {

    OTHER, TRACE, DEBUG, INFO, WARN, ERROR, FATAL;

    public static LogLevel parse(String token) {
        if (token == null) {
            return OTHER;
        }
        LogLevel level = ParseConstant.LEVEL_ALIASES.get(token.trim().toUpperCase(Locale.ROOT));
        return level == null ? OTHER : level;
    }

    public boolean isAtLeast(LogLevel minimum) {
        return ordinal() >= minimum.ordinal();
    }

    public boolean isError() {
        return isAtLeast(ERROR);
    }

    public boolean isWarning() {
        return this == WARN;
    }
}
