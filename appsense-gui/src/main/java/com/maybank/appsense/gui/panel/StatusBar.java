package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.DeploymentDetectedEvent;
import com.maybank.appsense.core.event.NoticeEvent;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.gui.GuiConstant;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thin status bar at the bottom of the frame.
 * Shows the last notice and deployment timestamp.
 */
public final class StatusBar extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JLabel noticeLabel;
    private final JLabel deployLabel;

    public StatusBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        setPreferredSize(new java.awt.Dimension(0, GuiConstant.STATUSBAR_HEIGHT));

        noticeLabel = new JLabel(MessageUtil.get(MessageKey.STATUS_READ_ONLY));
        noticeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        deployLabel = new JLabel();
        deployLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(noticeLabel, BorderLayout.CENTER);
        add(deployLabel, BorderLayout.EAST);
    }

    /** Called by EventBinder on EDT. */
    public void onConnectionState(ConnectionStateEvent event) {
        if (event.state() == ConnectionStateEvent.State.DISCONNECTED) {
            noticeLabel.setText(MessageUtil.get(MessageKey.STATUS_READ_ONLY));
        }
    }

    /** Called by EventBinder on EDT. */
    public void onDeployment(DeploymentDetectedEvent event) {
        ZonedDateTime zdt = event.marker().timestamp().atZone(ZoneId.systemDefault());
        deployLabel.setText(MessageUtil.get(MessageKey.STATUS_LAST_DEPLOYMENT, FMT.format(zdt)));
    }

    /** Called by EventBinder on EDT. */
    public void onNotice(NoticeEvent event) {
        noticeLabel.setText(event.text());
    }
}
