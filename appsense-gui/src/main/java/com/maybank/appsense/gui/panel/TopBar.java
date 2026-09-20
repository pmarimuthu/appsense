package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.ConnectionStateEvent;
import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.gui.GuiConstant;
import com.maybank.appsense.gui.MainFrame;
import com.maybank.appsense.gui.dialog.ProfileFormDialog;
import com.maybank.appsense.gui.util.AppIcon;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Top action bar: profile/log selectors, connect/disconnect, pause/resume, status chip.
 */
public final class TopBar extends JPanel {

    private final MainFrame frame;

    private final JComboBox<String> profileBox;
    private final JButton connectBtn;
    private final JButton pauseBtn;
    private final JLabel statusChip;
    private final JLabel profileLabel;
    private final JLabel logLabel;

    public TopBar(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setPreferredSize(new java.awt.Dimension(0, GuiConstant.TOPBAR_HEIGHT));

        profileLabel = new JLabel(MessageUtil.get(MessageKey.TOPBAR_PROFILE_LABEL));
        logLabel = new JLabel(MessageUtil.get(MessageKey.TOPBAR_LOG_LABEL));
        profileBox = new JComboBox<>();
        profileBox.setPrototypeDisplayValue("WWWWWWWWWWWWWW");

        connectBtn = new JButton(MessageUtil.get(MessageKey.TOPBAR_CONNECT));
        connectBtn.setIcon(AppIcon.toolbar("connect"));
        connectBtn.addActionListener(e -> onConnectClicked());

        pauseBtn = new JButton(MessageUtil.get(MessageKey.TOPBAR_PAUSE));
        pauseBtn.setIcon(AppIcon.toolbar("pause"));
        pauseBtn.setEnabled(false);
        pauseBtn.addActionListener(e -> onPauseClicked());

        JButton manageBtn = new JButton(AppIcon.toolbar("settings"));
        manageBtn.setToolTipText("Manage Profiles");
        manageBtn.addActionListener(e -> frame.openProfileManager());

        statusChip = new JLabel(MessageUtil.get(MessageKey.TOPBAR_STATUS_DISCONNECTED));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        left.add(profileLabel);
        left.add(profileBox);
        left.add(logLabel);
        left.add(connectBtn);
        left.add(manageBtn);
        left.add(Box.createHorizontalStrut(8));
        left.add(pauseBtn);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.add(statusChip);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        refreshProfiles();
    }

    /** Called by EventBinder on EDT when pause state changes. */
    public void onPauseState(PauseStateEvent event) {
        if (event.paused()) {
            pauseBtn.setText(MessageUtil.get(MessageKey.TOPBAR_RESUME));
            pauseBtn.setIcon(AppIcon.toolbar("resume"));
        } else {
            pauseBtn.setText(MessageUtil.get(MessageKey.TOPBAR_PAUSE));
            pauseBtn.setIcon(AppIcon.toolbar("pause"));
        }
    }

    /** Called by EventBinder on EDT when connection state changes. */
    public void onConnectionState(ConnectionStateEvent event) {
        String status = switch (event.state()) {
            case CONNECTED -> MessageUtil.get(MessageKey.TOPBAR_STATUS_LIVE);
            case CONNECTING, RECONNECTING -> MessageUtil.get(MessageKey.TOPBAR_STATUS_RECONNECTING);
            case DISCONNECTED -> MessageUtil.get(MessageKey.TOPBAR_STATUS_DISCONNECTED);
        };
        statusChip.setText(status);
        boolean connected = event.state() == ConnectionStateEvent.State.CONNECTED
                || event.state() == ConnectionStateEvent.State.RECONNECTING;
        connectBtn.setText(connected
                ? MessageUtil.get(MessageKey.TOPBAR_DISCONNECT)
                : MessageUtil.get(MessageKey.TOPBAR_CONNECT));
        connectBtn.setIcon(connected ? AppIcon.toolbar("disconnect") : AppIcon.toolbar("connect"));
        pauseBtn.setEnabled(connected);
    }

    public void setProfileAndLog(String profileName, String logLabel) {
        profileBox.setSelectedItem(profileName);
        this.logLabel.setText(logLabel);
    }

    private void onConnectClicked() {
        if (frame.getActiveSession() != null && frame.getActiveSession().isConnected()) {
            frame.disconnect();
            return;
        }
        String selected = (String) profileBox.getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            ProfileBean profile = frame.getConfigService().findProfile(selected);
            if (profile.getLogs().isEmpty()) {
                return;
            }
            frame.connect(profile, profile.getLogs().get(0));
        } catch (Exception ex) {
            // profile not found — refresh combo
            refreshProfiles();
        }
    }

    private void onPauseClicked() {
        if (frame.getActiveSession() == null) {
            return;
        }
        if (frame.getActiveSession().isPaused()) {
            frame.resume();
        } else {
            frame.pause();
        }
    }

    private void refreshProfiles() {
        profileBox.removeAllItems();
        List<ProfileBean> profiles = frame.getConfigService().getProfiles();
        for (ProfileBean p : profiles) {
            profileBox.addItem(p.getName());
        }
    }
}
