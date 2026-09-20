package com.maybank.appsense.core.model;

public enum LevelFilter {

    ALL(LogLevel.OTHER),
    WARN(LogLevel.WARN),
    ERROR(LogLevel.ERROR);

    private final LogLevel minimum;

    LevelFilter(LogLevel minimum) {
        this.minimum = minimum;
    }

    public boolean accepts(LogLevel level) {
        return level.isAtLeast(minimum);
    }
}
