package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.StatsUpdatedEvent;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.StatsSnapshotRecord;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Right-side panel showing error/warning counters and a sparkline of errors-per-minute.
 */
public final class StatsPanel extends JPanel {

    private final JLabel errorLabel;
    private final JLabel warnLabel;
    private final SparklinePanel sparkline;

    public StatsPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        errorLabel = new JLabel(MessageUtil.get(MessageKey.STATS_ERRORS, 0), SwingConstants.LEFT);
        warnLabel = new JLabel(MessageUtil.get(MessageKey.STATS_WARNINGS, 0), SwingConstants.LEFT);
        sparkline = new SparklinePanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridx = 0; gbc.gridy = 0;
        add(errorLabel, gbc);

        gbc.gridy = 1;
        add(warnLabel, gbc);

        gbc.gridy = 2;
        add(new JLabel(MessageUtil.get(MessageKey.STATS_NOW), SwingConstants.CENTER), gbc);

        gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        add(sparkline, gbc);
    }

    /** Called by EventBinder on EDT. */
    public void onStats(StatsUpdatedEvent event) {
        StatsSnapshotRecord snap = event.stats();
        errorLabel.setText(MessageUtil.get(MessageKey.STATS_ERRORS, snap.errorCount()));
        warnLabel.setText(MessageUtil.get(MessageKey.STATS_WARNINGS, snap.warningCount()));
        sparkline.setValues(snap.errorsPerMinute());
    }
}
