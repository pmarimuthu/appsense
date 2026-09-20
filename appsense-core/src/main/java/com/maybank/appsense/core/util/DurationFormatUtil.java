package com.maybank.appsense.core.util;

import com.maybank.appsense.core.config.AppConstant;
import java.time.Duration;

public final class DurationFormatUtil {

    private DurationFormatUtil() {
    }

    /** Formats a duration as dd:hh:mm. */
    public static String formatDdHhMm(Duration duration) {
        return AppConstant.ELAPSED_FORMAT.formatted(
                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart());
    }
}
