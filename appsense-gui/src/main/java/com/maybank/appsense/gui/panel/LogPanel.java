package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.GapMarkerEvent;
import com.maybank.appsense.core.event.LineAddedEvent;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.gui.GuiConstant;
import com.maybank.appsense.gui.model.LogTableModel;
import com.maybank.appsense.gui.renderer.LogLineRenderer;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.util.List;

/**
 * Main log display panel: scrollable table + collapsible stack-trace sub-panel.
 */
public final class LogPanel extends JPanel {

    private final LogTableModel model;
    private final JTable table;
    private final StackTracePanel stackPanel;
    private final JScrollPane scrollPane;
    private boolean autoScroll = true;

    public LogPanel() {
        super(new BorderLayout());
        model = new LogTableModel();
        table = new JTable(model);
        stackPanel = new StackTracePanel();

        configureTable();

        scrollPane = new JScrollPane(table);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this::onScroll);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, stackPanel);
        split.setResizeWeight(1.0);
        split.setDividerSize(4);

        add(split, BorderLayout.CENTER);
    }

    /** Called by EventBinder on EDT. */
    public void onLineAdded(LineAddedEvent event) {
        if (event.reset()) {
            model.reset(event.items());
        } else {
            model.append(event.items());
        }
        if (autoScroll) {
            scrollToBottom();
        }
    }

    /** Called by EventBinder on EDT — gap marker is already in the LineAddedEvent stream. */
    public void onGapMarker(GapMarkerEvent event) {
        // gap marker delivered via LineAddedEvent; nothing extra needed here
    }

    private void configureTable() {
        table.setRowHeight(GuiConstant.LOG_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(StreamItem.class, new LogLineRenderer());
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        table.getColumnModel().getColumn(LogTableModel.COL_TIME)
                .setPreferredWidth(GuiConstant.COL_TIME_WIDTH);
        table.getColumnModel().getColumn(LogTableModel.COL_LEVEL)
                .setPreferredWidth(GuiConstant.COL_LEVEL_WIDTH);
        table.getColumnModel().getColumn(LogTableModel.COL_LOGGER)
                .setPreferredWidth(GuiConstant.COL_LOGGER_WIDTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = table.getSelectedRow();
            if (row < 0 || row >= model.getRowCount()) {
                stackPanel.show(null);
                return;
            }
            StreamItem item = model.getRow(row);
            if (item instanceof LogLineRecord rec) {
                stackPanel.show(rec);
            } else {
                stackPanel.show(null);
            }
        });
    }

    private void scrollToBottom() {
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        }
    }

    private void onScroll(AdjustmentEvent e) {
        JScrollBar bar = (JScrollBar) e.getAdjustable();
        int max = bar.getMaximum() - bar.getVisibleAmount();
        autoScroll = bar.getValue() >= max - 5;
    }
}
