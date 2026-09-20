package com.maybank.appsense.gui.renderer;

import com.maybank.appsense.core.model.DeploymentMarkerRecord;
import com.maybank.appsense.core.model.GapMarkerRecord;
import com.maybank.appsense.core.model.LogLevel;
import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.StreamItem;
import com.maybank.appsense.gui.model.LogTableModel;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Single renderer for all log table columns.
 * Registered as the default renderer for {@link Object} on the log table.
 * Extracts the correct field from the {@link StreamItem} by column index.
 */
public final class LogLineRenderer extends DefaultTableCellRenderer {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final Color COLOR_ERROR = new Color(0xd45f5f);
    private static final Color COLOR_WARN = new Color(0xc8a93a);
    private static final Color COLOR_OTHER = new Color(0x888888);
    private static final Color COLOR_DEPLOY = new Color(0x3a8a6e);
    private static final Color COLOR_GAP = new Color(0x777777);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);

        if (!(value instanceof StreamItem item)) {
            return this;
        }

        String text = switch (item) {
            case LogLineRecord rec -> cellText(rec, column);
            case DeploymentMarkerRecord dep -> column == LogTableModel.COL_MESSAGE
                    ? "── Deployment ──" : "";
            case GapMarkerRecord gap -> column == LogTableModel.COL_MESSAGE
                    ? "── Log gap ──" : "";
        };
        setText(text);

        if (!isSelected) {
            Color fg = switch (item) {
                case LogLineRecord rec -> levelColor(rec.level());
                case DeploymentMarkerRecord d -> COLOR_DEPLOY;
                case GapMarkerRecord g -> COLOR_GAP;
            };
            setForeground(fg);
        }
        return this;
    }

    private static String cellText(LogLineRecord rec, int col) {
        return switch (col) {
            case LogTableModel.COL_TIME -> {
                ZonedDateTime zdt = rec.timestamp().atZone(ZoneId.systemDefault());
                yield FMT.format(zdt);
            }
            case LogTableModel.COL_LEVEL -> rec.level().name();
            case LogTableModel.COL_LOGGER -> rec.logger();
            case LogTableModel.COL_MESSAGE -> rec.message();
            default -> "";
        };
    }

    private static Color levelColor(LogLevel level) {
        return switch (level) {
            case ERROR -> COLOR_ERROR;
            case WARN -> COLOR_WARN;
            case OTHER -> COLOR_OTHER;
            default -> null;
        };
    }
}
