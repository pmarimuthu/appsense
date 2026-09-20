package com.maybank.appsense.cli.screen;

import com.maybank.appsense.cli.CliConstant;
import com.maybank.appsense.cli.color.AnsiColorUtil;
import com.maybank.appsense.core.model.StatsSnapshotRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Renders the fixed-width stats panel shown to the right of the log pane.
 * All lines are padded to {@link CliConstant#STATS_PANE_WIDTH}.
 */
public final class StatsPane {

    private static final String[] SPARKLINE_CHARS = {" ", "▁", "▂", "▃",
            "▄", "▅", "▆", "▇", "█"};

    private StatsSnapshotRecord snapshot;

    public void update(StatsSnapshotRecord snapshot) {
        this.snapshot = snapshot;
    }

    /** Returns lines of exactly {@link CliConstant#STATS_PANE_WIDTH} visible chars. */
    public List<String> render() {
        if (snapshot == null) {
            return Collections.emptyList();
        }
        List<String> lines = new ArrayList<>();
        int w = CliConstant.STATS_PANE_WIDTH;
        lines.add(pad("  STATS", w));
        lines.add(pad("", w));
        lines.add(AnsiColorUtil.ERROR + pad("  Errors   " + snapshot.errorCount(), w) + AnsiColorUtil.RESET);
        lines.add(AnsiColorUtil.WARN  + pad("  Warnings " + snapshot.warningCount(), w) + AnsiColorUtil.RESET);
        lines.add(pad("", w));
        lines.add(pad("  " + sparkline(snapshot.errorsPerMinute()) + " now", w));
        return lines;
    }

    private String sparkline(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        int bars = Math.min(values.size(), CliConstant.SPARKLINE_BARS);
        List<Integer> slice = values.subList(values.size() - bars, values.size());
        int max = slice.stream().mapToInt(Integer::intValue).max().orElse(1);
        if (max == 0) {
            max = 1;
        }
        StringBuilder sb = new StringBuilder();
        for (int v : slice) {
            int idx = (v * (SPARKLINE_CHARS.length - 1)) / max;
            sb.append(SPARKLINE_CHARS[idx]);
        }
        return sb.toString();
    }

    private String pad(String s, int width) {
        if (s.length() >= width) {
            return s.substring(0, width);
        }
        return s + " ".repeat(width - s.length());
    }
}
