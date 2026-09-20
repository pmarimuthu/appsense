package com.maybank.appsense.core.summary;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.FilterConstant;
import com.maybank.appsense.core.config.ParseConstant;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.ExceptionInfoRecord;
import com.maybank.appsense.core.model.LogLineRecord;
import java.util.List;
import java.util.regex.Matcher;

public final class ExceptionExtractUtil {

    private ExceptionExtractUtil() {
    }

    /**
     * Reduces a logged error to its root-cause type and message plus the first application frame.
     * Errors without a stack trace fall back to the log message.
     */
    public static ExceptionInfoRecord extract(LogLineRecord line, List<String> appPackages) {
        List<String> trace = line.stackTrace();
        if (trace.isEmpty()) {
            return new ExceptionInfoRecord(
                    MessageUtil.get(MessageKey.SUMMARY_NO_EXCEPTION), clip(line.message()), "");
        }
        int headerIndex = rootCauseIndex(trace);
        String header = trace.get(headerIndex).trim();
        if (header.startsWith(ParseConstant.CAUSED_BY_PREFIX)) {
            header = header.substring(ParseConstant.CAUSED_BY_PREFIX.length());
        }
        int split = header.indexOf(ParseConstant.TYPE_MESSAGE_SEPARATOR);
        String type = split < 0 ? header : header.substring(0, split).trim();
        String message = split < 0 ? "" : header.substring(split + 1).trim();
        return new ExceptionInfoRecord(type, clip(message), firstAppFrame(trace, headerIndex, appPackages));
    }

    private static int rootCauseIndex(List<String> trace) {
        int firstHeader = AppConstant.NOT_FOUND;
        int lastCause = AppConstant.NOT_FOUND;
        for (int index = 0; index < trace.size(); index++) {
            String text = trace.get(index);
            if (text.startsWith(ParseConstant.CAUSED_BY_PREFIX)) {
                lastCause = index;
            } else if (firstHeader < 0 && !isFrameLike(text)) {
                firstHeader = index;
            }
        }
        if (lastCause >= 0) {
            return lastCause;
        }
        return Math.max(firstHeader, 0);
    }

    private static boolean isFrameLike(String text) {
        return ParseConstant.FRAME_LINE.matcher(text).matches() || ParseConstant.ELISION_LINE.matcher(text).matches();
    }

    private static String firstAppFrame(List<String> trace, int headerIndex, List<String> appPackages) {
        String rootFrame = scanFrames(trace, headerIndex + 1, appPackages);
        return rootFrame.isEmpty() ? scanFrames(trace, 0, appPackages) : rootFrame;
    }

    private static String scanFrames(List<String> trace, int from, List<String> appPackages) {
        for (int index = from; index < trace.size(); index++) {
            Matcher frame = ParseConstant.FRAME_LINE.matcher(trace.get(index));
            if (frame.matches() && belongsToApp(frame.group(ParseConstant.GROUP_FRAME), appPackages)) {
                return frame.group(ParseConstant.GROUP_FRAME).trim();
            }
        }
        return "";
    }

    private static boolean belongsToApp(String frame, List<String> appPackages) {
        if (appPackages.isEmpty()) {
            return true;
        }
        int argsStart = frame.indexOf(ParseConstant.FRAME_ARGS_START);
        String qualifiedMethod = argsStart < 0 ? frame : frame.substring(0, argsStart);
        return appPackages.stream().anyMatch(qualifiedMethod::startsWith);
    }

    private static String clip(String text) {
        return text.length() <= FilterConstant.SUMMARY_MESSAGE_MAX_LENGTH
                ? text : text.substring(0, FilterConstant.SUMMARY_MESSAGE_MAX_LENGTH);
    }
}
