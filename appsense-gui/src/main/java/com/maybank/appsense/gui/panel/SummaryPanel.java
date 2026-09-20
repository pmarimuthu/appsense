package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.StatsUpdatedEvent;
import com.maybank.appsense.gui.GuiConstant;
import com.maybank.appsense.gui.model.SummaryTableModel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;

/**
 * Bottom-right panel displaying the exception summary table.
 */
public final class SummaryPanel extends JPanel {

    private final SummaryTableModel model;

    public SummaryPanel() {
        super(new BorderLayout());
        model = new SummaryTableModel();

        JTable table = new JTable(model);
        table.setRowHeight(GuiConstant.LOG_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        table.getColumnModel().getColumn(SummaryTableModel.COL_COUNT).setPreferredWidth(36);
        table.getColumnModel().getColumn(SummaryTableModel.COL_FIRST).setPreferredWidth(70);
        table.getColumnModel().getColumn(SummaryTableModel.COL_LAST).setPreferredWidth(70);

        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new java.awt.Dimension(0, GuiConstant.SUMMARY_PANEL_HEIGHT));
    }

    /** Called by EventBinder on EDT. */
    public void onStats(StatsUpdatedEvent event) {
        model.setRows(event.summary());
    }
}
