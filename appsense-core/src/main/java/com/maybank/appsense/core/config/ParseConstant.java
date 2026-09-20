package com.maybank.appsense.core.config;

import com.maybank.appsense.core.model.LogLevel;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ParseConstant {

    public static final String GROUP_DATE = "date";
    public static final String GROUP_TIME = "time";
    public static final String GROUP_MILLIS = "millis";
    public static final String GROUP_LEVEL = "level";
    public static final String GROUP_LOGGER = "logger";
    public static final String GROUP_BRACKET_LOGGER = "bracketLogger";
    public static final String GROUP_MESSAGE = "message";
    public static final String GROUP_STAMP = "stamp";
    public static final String GROUP_MESSAGE_ID = "messageId";
    public static final String GROUP_MONTH = "month";
    public static final String GROUP_DAY = "day";
    public static final String GROUP_YEAR = "year";
    public static final String GROUP_HOUR = "hour";
    public static final String GROUP_MINUTE = "minute";
    public static final String GROUP_SECOND = "second";
    public static final String GROUP_MERIDIEM = "meridiem";
    public static final String GROUP_FRAME = "frame";

    public static final long NANOS_PER_MILLI = 1_000_000L;
    public static final int HOURS_PER_HALF_DAY = 12;
    public static final String MERIDIEM_PM = "PM";
    public static final String MESSAGE_ID_SEPARATOR = ": ";
    public static final int MAX_STACK_LINES = 500;

    private static final String TIMESTAMP_FRAGMENT =
            "(?<date>\\d{4}-\\d{2}-\\d{2})[ T](?<time>\\d{2}:\\d{2}:\\d{2})(?:[,.](?<millis>\\d{3}))?";

    public static final Pattern JBOSS_LINE = Pattern.compile(
            "^" + TIMESTAMP_FRAGMENT
                    + "\\s+(?<level>[A-Za-z]+)\\s+\\[(?<logger>[^\\]]*)\\]\\s+\\([^)]*\\)\\s+(?<message>.*)$");

    public static final Pattern GENERIC_LINE = Pattern.compile(
            "^" + TIMESTAMP_FRAGMENT
                    + "\\s+(?:\\[[^\\]]*\\]\\s+)?"
                    + "(?<level>TRACE|DEBUG|INFO|WARN|WARNING|ERROR|FATAL|SEVERE)\\b\\s*"
                    + "(?:\\[(?<bracketLogger>[^\\]]*)\\]\\s*)?(?:\\([^)]*\\)\\s*)?"
                    + "(?:(?<logger>[\\w$]+(?:\\.[\\w$]+)*)\\s+-\\s+)?(?<message>.*)$");

    public static final Pattern WEBLOGIC_LINE = Pattern.compile(
            "^####<(?<stamp>[^>]*)>\\s+<(?<level>[^>]*)>\\s+<(?<logger>[^>]*)>"
                    + "(?:\\s+<.*?>){7}\\s+<(?<messageId>[^>]*)>\\s+<(?<message>.*)>\\s*$");

    public static final Pattern WEBLOGIC_STAMP = Pattern.compile(
            "(?<month>[A-Za-z]{3})[A-Za-z]*\\s+(?<day>\\d{1,2}),?\\s+(?<year>\\d{4}),?\\s+"
                    + "(?<hour>\\d{1,2}):(?<minute>\\d{2}):(?<second>\\d{2})"
                    + "(?:[:,.](?<millis>\\d{1,3}))?\\s*(?<meridiem>[AaPp][Mm])?");

    public static final List<String> MONTH_ABBREVIATIONS = List.of(
            "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC");

    public static final Pattern FRAME_LINE = Pattern.compile("^\\s+at\\s+(?<frame>\\S.*)$");
    public static final Pattern ELISION_LINE = Pattern.compile("^\\s+\\.\\.\\.\\s+\\d+\\s+\\w+.*$");
    public static final String CAUSED_BY_PREFIX = "Caused by: ";
    public static final char TYPE_MESSAGE_SEPARATOR = ':';
    public static final char FRAME_ARGS_START = '(';

    public static final Map<String, LogLevel> LEVEL_ALIASES = Map.ofEntries(
            Map.entry("TRACE", LogLevel.TRACE),
            Map.entry("DEBUG", LogLevel.DEBUG),
            Map.entry("INFO", LogLevel.INFO),
            Map.entry("NOTICE", LogLevel.INFO),
            Map.entry("WARN", LogLevel.WARN),
            Map.entry("WARNING", LogLevel.WARN),
            Map.entry("ERROR", LogLevel.ERROR),
            Map.entry("SEVERE", LogLevel.ERROR),
            Map.entry("FATAL", LogLevel.FATAL),
            Map.entry("CRITICAL", LogLevel.FATAL),
            Map.entry("ALERT", LogLevel.FATAL),
            Map.entry("EMERGENCY", LogLevel.FATAL));

    private ParseConstant() {
    }
}
