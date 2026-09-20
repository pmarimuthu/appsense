package com.maybank.appsense.gui.model;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.StreamItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for the main log view.
 * {@code getValueAt()} returns the raw {@link StreamItem} for every column;
 * the renderer extracts the correct field by column index.
 */
public final class LogTableModel extends AbstractTableModel {

    public static final int COL_TIME = 0;
    public static final int COL_LEVEL = 1;
    public static final int COL_LOGGER = 2;
    public static final int COL_MESSAGE = 3;
    public static final int COL_COUNT = 4;

    private static final String[] HEADERS = {
            MessageUtil.get(MessageKey.LOG_COL_TIME),
            MessageUtil.get(MessageKey.LOG_COL_LEVEL),
            MessageUtil.get(MessageKey.LOG_COL_LOGGER),
            MessageUtil.get(MessageKey.LOG_COL_MESSAGE)
    };

    private final List<StreamItem> rows = new ArrayList<>();

    public void reset(List<StreamItem> items) {
        rows.clear();
        rows.addAll(items);
        fireTableDataChanged();
    }

    public void append(List<StreamItem> items) {
        if (items.isEmpty()) {
            return;
        }
        int first = rows.size();
        rows.addAll(items);
        fireTableRowsInserted(first, rows.size() - 1);
    }

    public StreamItem getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COL_COUNT;
    }

    @Override
    public String getColumnName(int column) {
        return HEADERS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return StreamItem.class;
    }

    /**
     * Returns the raw {@link StreamItem} for every column so the renderer can
     * pattern-match on type and extract the right field.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex);
    }
}
