package com.maybank.appsense.cli.screen;

import com.maybank.appsense.cli.CliConstant;
import com.maybank.appsense.cli.color.AnsiColorUtil;
import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.core.model.SummaryRowRecord;
import com.maybank.appsense.core.model.StatsSnapshotRecord;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;

/**
 * Owns the terminal and redraws the full screen on every event.
 * All public mutators are thread-safe; rendering is serialised with a lock.
 */
public final class FullScreenRenderer {

    private final Terminal terminal;
    private final PrintWriter out;
    private final LogPane logPane;
    private final StatsPane statsPane;
    private final SummaryPane summaryPane;
    private final KeyLegend keyLegend;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean dumb;

    private String profileName = "";
    private String logLabel    = "";
    private String statusText  = CliConstant.STATUS_DISCONN;
    private boolean paused;
    private String pauseInfo   = "";
    private LevelFilter levelFilter = LevelFilter.ALL;
    private String keyword     = "";

    public FullScreenRenderer(Terminal terminal) {
        this.terminal   = terminal;
        this.out        = terminal.writer();
        this.logPane    = new LogPane();
        this.statsPane  = new StatsPane();
        this.summaryPane = new SummaryPane();
        this.keyLegend  = new KeyLegend();
        this.dumb       = terminal.getType().equals("dumb");
    }

    public LogPane logPane()         { return logPane; }
    public SummaryPane summaryPane() { return summaryPane; }

    public void setProfile(String profileName, String logLabel) {
        lock.lock();
        try { this.profileName = profileName; this.logLabel = logLabel; } finally { lock.unlock(); }
    }

    public void onConnectionState(ConnectionStateEvent event) {
        lock.lock();
        try {
            statusText = switch (event.state()) {
                case CONNECTED    -> CliConstant.STATUS_LIVE;
                case RECONNECTING -> CliConstant.STATUS_RECONN;
                default           -> CliConstant.STATUS_DISCONN;
            };
        } finally { lock.unlock(); }
        redraw();
    }

    public void onPauseState(PauseStateEvent event) {
        lock.lock();
        try {
            paused = event.paused();
            if (paused) {
                pauseInfo = CliConstant.BULLET_PAUSE + " Paused " + event.elapsedText()
                        + " · " + event.newLines() + " new lines"
                        + (event.hasDeployment() ? " · Deploy detected" : "");
            } else {
                pauseInfo = "";
                logPane.resumeAutoScroll();
            }
        } finally { lock.unlock(); }
        redraw();
    }

    public void onStats(StatsSnapshotRecord snap, List<SummaryRowRecord> rows) {
        lock.lock();
        try { statsPane.update(snap); summaryPane.update(rows); } finally { lock.unlock(); }
        redraw();
    }

    public void resetLines(List<StreamItem> items) {
        lock.lock();
        try { logPane.reset(items); } finally { lock.unlock(); }
        redraw();
    }

    public void onLines(List<StreamItem> items) {
        lock.lock();
        try { logPane.append(items); } finally { lock.unlock(); }
        redraw();
    }

    public void setLevelFilter(LevelFilter lf) {
        lock.lock();
        try { this.levelFilter = lf; } finally { lock.unlock(); }
        redraw();
    }

    public void setKeyword(String kw) {
        lock.lock();
        try { this.keyword = kw == null ? "" : kw; } finally { lock.unlock(); }
        redraw();
    }

    public void scrollUp()                { lock.lock(); try { logPane.scrollUp(); } finally { lock.unlock(); } redraw(); }
    public void scrollDown(int rows)      { lock.lock(); try { logPane.scrollDown(rows); } finally { lock.unlock(); } redraw(); }
    public void toggleExpand(int row)     { lock.lock(); try { logPane.toggleExpand(row); } finally { lock.unlock(); } redraw(); }

    /** Clears the screen and redraws all panes. */
    public void redraw() {
        if (dumb) {
            return;
        }
        lock.lock();
        try {
            int rows = terminal.getHeight();
            int cols = terminal.getWidth();
            int statsW = CliConstant.STATS_PANE_WIDTH;
            int logW   = cols - statsW - 1;
            int summaryH = CliConstant.SUMMARY_ROWS + 2;
            int topLines = paused ? 3 : 2;
            int logH = rows - topLines - summaryH - 1;

            terminal.puts(InfoCmp.Capability.clear_screen);
            printTopBar(cols);
            printFilterBar(cols);
            if (paused) {
                out.println(AnsiColorUtil.WARN + pauseInfo + AnsiColorUtil.RESET);
            }
            printLogAndStats(logH, logW, statsW, cols);
            printSummarySection(cols);
            out.print(keyLegend.render());
            out.flush();
        } finally {
            lock.unlock();
        }
    }

    private void printTopBar(int cols) {
        String bar = "  " + AnsiColorUtil.BOLD + profileName + AnsiColorUtil.RESET
                + " | " + logLabel + " | "
                + AnsiColorUtil.DEPLOY + CliConstant.BULLET_LIVE + " " + statusText + AnsiColorUtil.RESET;
        out.println(bar);
    }

    private void printFilterBar(int cols) {
        String kw = keyword.isEmpty() ? "_" : keyword;
        String bar = "  Level: " + levelFilter.name()
                + "  Search: " + kw
                + "  [d] Since deploy  [x] Discard  [m] Mark";
        out.println(AnsiColorUtil.DIM + bar + AnsiColorUtil.RESET);
    }

    private void printLogAndStats(int logH, int logW, int statsW, int cols) {
        out.println(AnsiColorUtil.DIM + CliConstant.SEPARATOR.repeat(logW)
                + CliConstant.TEE_DOWN + CliConstant.SEPARATOR.repeat(statsW) + AnsiColorUtil.RESET);
        List<String> logLines  = logPane.render(logH, logW);
        List<String> statLines = statsPane.render();
        for (int i = 0; i < logH; i++) {
            String ll = i < logLines.size()  ? logLines.get(i)  : "";
            String sl = i < statLines.size() ? statLines.get(i) : "";
            out.println(ll + AnsiColorUtil.DIM + CliConstant.PIPE + AnsiColorUtil.RESET + sl);
        }
        out.println(AnsiColorUtil.DIM + CliConstant.SEPARATOR.repeat(logW)
                + CliConstant.TEE_UP + CliConstant.SEPARATOR.repeat(statsW) + AnsiColorUtil.RESET);
    }

    private void printSummarySection(int cols) {
        List<String> sumLines = summaryPane.render(cols);
        for (String line : sumLines) {
            out.println(line);
        }
    }

    public void close() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        out.flush();
    }
}
