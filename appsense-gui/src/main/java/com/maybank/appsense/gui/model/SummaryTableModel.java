package com.maybank.appsense.gui.model;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.SummaryRowRecord;

import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Table model for the exception summary table. */
public final class SummaryTableModel extends AbstractTableModel {

    public static final int COL_COUNT = 0;
    public static final int COL_EXCEPTION = 1;
    public static final int COL_MESSAGE = 2;
    public static final int COL_LOCATION = 3;
    public static final int COL_FIRST = 4;
    public static final int COL_LAST = 5;
    public static final int COL_TOTAL = 6;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String[] HEADERS = {
            MessageUtil.get(MessageKey.SUMMARY_COL_COUNT),
            MessageUtil.get(MessageKey.SUMMARY_COL_EXCEPTION),
            MessageUtil.get(MessageKey.SUMMARY_COL_MESSAGE),
            MessageUtil.get(MessageKey.SUMMARY_COL_LOCATION),
            MessageUtil.get(MessageKey.SUMMARY_COL_FIRST),
            MessageUtil.get(MessageKey.SUMMARY_COL_LAST)
    };

    private final List<SummaryRowRecord> rows = new ArrayList<>();

    public void setRows(List<SummaryRowRecord> newRows) {
        rows.clear();
        rows.addAll(newRows);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COL_TOTAL;
    }

    @Override
    public String getColumnName(int column) {
        return HEADERS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SummaryRowRecord row = rows.get(rowIndex);
        return switch (columnIndex) {
            case COL_COUNT -> row.count();
            case COL_EXCEPTION -> row.exceptionType();
            case COL_MESSAGE -> row.message();
            case COL_LOCATION -> row.location();
            case COL_FIRST -> fmt(row);
            case COL_LAST -> fmtLast(row);
            default -> "";
        };
    }

    private static String fmt(SummaryRowRecord row) {
        ZonedDateTime zdt = row.firstSeen().atZone(ZoneId.systemDefault());
        return FMT.format(zdt);
    }

    private static String fmtLast(SummaryRowRecord row) {
        ZonedDateTime zdt = row.lastSeen().atZone(ZoneId.systemDefault());
        return FMT.format(zdt);
    }
}
