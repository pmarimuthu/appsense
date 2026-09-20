package com.maybank.appsense.cli.screen;

import com.maybank.appsense.cli.color.AnsiColorUtil;
import com.maybank.appsense.core.model.SummaryRowRecord;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Renders the error-summary table at the bottom of the screen.
 */
public final class SummaryPane {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int COL_COUNT  = 6;
    private static final int COL_TYPE   = 22;
    private static final int COL_MSG    = 28;
    private static final int COL_LOC    = 24;
    private static final int COL_TIME   = 9;

    private List<SummaryRowRecord> rows = Collections.emptyList();
    private int selectedRow = -1;

    public void update(List<SummaryRowRecord> rows) {
        this.rows = rows == null ? Collections.emptyList() : rows;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int rowCount() {
        return rows.size();
    }

    /** Returns the raw text of the selected row (for clipboard copy). */
    public String selectedText() {
        if (selectedRow < 0 || selectedRow >= rows.size()) {
            return "";
        }
        SummaryRowRecord r = rows.get(selectedRow);
        return r.exceptionType() + ": " + r.message() + " @ " + r.location();
    }

    public List<String> render(int terminalWidth) {
        List<String> lines = new ArrayList<>();
        lines.add(AnsiColorUtil.DIM + header(terminalWidth) + AnsiColorUtil.RESET);
        for (int i = 0; i < rows.size(); i++) {
            lines.add(row(rows.get(i), i == selectedRow, terminalWidth));
        }
        return lines;
    }

    private String header(int w) {
        return trunc(" Count", COL_COUNT) + "  "
                + trunc("Exception", COL_TYPE) + "  "
                + trunc("Message", COL_MSG) + "  "
                + trunc("Location", COL_LOC) + "  "
                + trunc("First", COL_TIME) + "  "
                + trunc("Last", COL_TIME);
    }

    private String row(SummaryRowRecord r, boolean selected, int w) {
        String line = trunc(" " + r.count(), COL_COUNT) + "  "
                + trunc(r.exceptionType(), COL_TYPE) + "  "
                + trunc(r.message(), COL_MSG) + "  "
                + trunc(r.location(), COL_LOC) + "  "
                + trunc(fmt(r), COL_TIME) + "  "
                + trunc(fmtLast(r), COL_TIME);
        if (selected) {
            return AnsiColorUtil.BOLD + AnsiColorUtil.ERROR + line + AnsiColorUtil.RESET;
        }
        return AnsiColorUtil.ERROR + line + AnsiColorUtil.RESET;
    }

    private String fmt(SummaryRowRecord r) {
        return r.firstSeen() == null ? "" : FMT.format(r.firstSeen().atZone(ZoneId.systemDefault()));
    }

    private String fmtLast(SummaryRowRecord r) {
        return r.lastSeen() == null ? "" : FMT.format(r.lastSeen().atZone(ZoneId.systemDefault()));
    }

    private String trunc(String s, int max) {
        if (s == null) {
            s = "";
        }
        if (s.length() > max) {
            return s.substring(0, max - 1) + "…";
        }
        return s + " ".repeat(max - s.length());
    }
}
