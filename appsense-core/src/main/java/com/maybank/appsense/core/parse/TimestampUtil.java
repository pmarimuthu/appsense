package com.maybank.appsense.core.parse;

import com.maybank.appsense.core.config.ParseConstant;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Matcher;

public final class TimestampUtil {

    private TimestampUtil() {
    }

    public static Instant fromIsoParts(Matcher matcher) {
        LocalDateTime local = LocalDateTime.of(
                LocalDate.parse(matcher.group(ParseConstant.GROUP_DATE)),
                LocalTime.parse(matcher.group(ParseConstant.GROUP_TIME)));
        return toInstant(local, matcher.group(ParseConstant.GROUP_MILLIS));
    }

    public static Instant fromWebLogicStamp(Matcher matcher) {
        int month = ParseConstant.MONTH_ABBREVIATIONS.indexOf(
                matcher.group(ParseConstant.GROUP_MONTH).toUpperCase(Locale.ROOT)) + 1;
        int hour = Integer.parseInt(matcher.group(ParseConstant.GROUP_HOUR));
        hour = applyMeridiem(hour, matcher.group(ParseConstant.GROUP_MERIDIEM));
        LocalDateTime local = LocalDateTime.of(
                Integer.parseInt(matcher.group(ParseConstant.GROUP_YEAR)),
                month,
                Integer.parseInt(matcher.group(ParseConstant.GROUP_DAY)),
                hour,
                Integer.parseInt(matcher.group(ParseConstant.GROUP_MINUTE)),
                Integer.parseInt(matcher.group(ParseConstant.GROUP_SECOND)));
        return toInstant(local, matcher.group(ParseConstant.GROUP_MILLIS));
    }

    private static int applyMeridiem(int hour, String meridiem) {
        if (meridiem == null) {
            return hour;
        }
        int base = hour % ParseConstant.HOURS_PER_HALF_DAY;
        boolean afternoon = meridiem.toUpperCase(Locale.ROOT).equals(ParseConstant.MERIDIEM_PM);
        return afternoon ? base + ParseConstant.HOURS_PER_HALF_DAY : base;
    }

    private static Instant toInstant(LocalDateTime local, String millis) {
        LocalDateTime adjusted = millis == null
                ? local
                : local.plusNanos(Long.parseLong(millis) * ParseConstant.NANOS_PER_MILLI);
        return adjusted.atZone(ZoneId.systemDefault()).toInstant();
    }
}
