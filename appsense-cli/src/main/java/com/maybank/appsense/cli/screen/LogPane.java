package com.maybank.appsense.cli.screen;

import com.maybank.appsense.cli.CliConstant;
import com.maybank.appsense.cli.color.AnsiColorUtil;
import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.GapMarkerRecord;
import com.maybank.appsense.core.model.LogLevel;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StreamItem;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders log lines for the left pane. Owns the scroll offset.
 */
public final class LogPane {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final List<StreamItem> items = new ArrayList<>();
    private int scrollOffset;
    private boolean autoScroll = true;
    private boolean expanded;
    private int expandedIndex = -1;

    public void append(List<StreamItem> newItems) {
        items.addAll(newItems);
        if (autoScroll) {
            scrollOffset = Math.max(0, items.size() - 1);
        }
    }

    public void reset(List<StreamItem> snapshot) {
        items.clear();
        items.addAll(snapshot);
        scrollOffset = Math.max(0, items.size() - 1);
        autoScroll = true;
    }

    public void scrollUp() {
        if (scrollOffset > 0) {
            scrollOffset--;
            autoScroll = false;
        }
    }

    public void scrollDown(int visibleRows) {
        if (scrollOffset < items.size() - 1) {
            scrollOffset++;
        }
        if (scrollOffset >= items.size() - visibleRows) {
            autoScroll = true;
        }
    }

    public void resumeAutoScroll() {
        autoScroll = true;
        scrollOffset = Math.max(0, items.size() - 1);
    }

    public void toggleExpand(int row) {
        if (expandedIndex == row && expanded) {
            expanded = false;
            expandedIndex = -1;
        } else {
            expandedIndex = row;
            expanded = true;
        }
    }

    /** Renders {@code visibleRows} lines of log output into a list. */
    public List<String> render(int visibleRows, int logWidth) {
        List<String> lines = new ArrayList<>();
        int start = Math.max(0, scrollOffset - visibleRows + 1);
        for (int i = start; i < items.size() && lines.size() < visibleRows; i++) {
            StreamItem item = items.get(i);
            switch (item) {
                case LogLineRecord r -> {
                    lines.add(formatLog(r, logWidth));
                    if (expanded && i == expandedIndex && !r.stackTrace().isEmpty()) {
                        for (String frame : r.stackTrace()) {
                            if (lines.size() >= visibleRows) break;
                            lines.add(AnsiColorUtil.DIM + "  " + trunc(frame, logWidth - 2) + AnsiColorUtil.RESET);
                        }
                    }
                }
                case DeploymentMarkerRecord d ->
                    lines.add(AnsiColorUtil.DEPLOY + trunc("── Deploy " + fmtTs(d) + " ──", logWidth) + AnsiColorUtil.RESET);
                case GapMarkerRecord g ->
                    lines.add(AnsiColorUtil.DIM + trunc("── gap ──", logWidth) + AnsiColorUtil.RESET);
            }
        }
        while (lines.size() < visibleRows) {
            lines.add("");
        }
        return lines;
    }

    private String formatLog(LogLineRecord r, int width) {
        String time  = FMT.format(r.timestamp().atZone(ZoneId.systemDefault()));
        String level = padRight(r.level().name(), CliConstant.LOG_LEVEL_WIDTH);
        String logger = trunc(r.logger(), CliConstant.LOG_LOGGER_WIDTH);
        int msgWidth = width - CliConstant.LOG_TIME_WIDTH - CliConstant.LOG_LEVEL_WIDTH
                - CliConstant.LOG_LOGGER_WIDTH - 3;
        String msg = trunc(r.message(), Math.max(msgWidth, 1));
        String color = levelColor(r.level());
        return (color != null ? color : "") + time + " " + level + " " + logger + " " + msg
                + (color != null ? AnsiColorUtil.RESET : "");
    }

    private String levelColor(LogLevel lv) {
        return switch (lv) {
            case ERROR -> AnsiColorUtil.ERROR;
            case WARN  -> AnsiColorUtil.WARN;
            default    -> null;
        };
    }

    private String fmtTs(DeploymentMarkerRecord d) {
        return FMT.format(d.timestamp().atZone(ZoneId.systemDefault()));
    }

    private String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    private String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
