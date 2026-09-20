package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.event.PauseStateEvent;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.gui.GuiConstant;
import com.maybank.appsense.gui.MainFrame;
import com.maybank.appsense.gui.dialog.ConfirmDialog;
import com.maybank.appsense.gui.util.AppIcon;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Yellow notification bar shown only while the stream is paused.
 * Displays elapsed time, new-line count, and optional deployment detection notice.
 */
public final class PauseBar extends JPanel {

    private final MainFrame frame;
    private final JLabel infoLabel;
    private final JButton discardBtn;
    private final JButton resumeBtn;
    private final JButton sinceBtn;

    public PauseBar(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setPreferredSize(new java.awt.Dimension(0, GuiConstant.PAUSEBAR_HEIGHT));
        setVisible(false);

        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT);

        sinceBtn = new JButton(MessageUtil.get(MessageKey.FILTER_SINCE_LAST_DEPLOYMENT));
        sinceBtn.setVisible(false);

        discardBtn = new JButton(MessageUtil.get(MessageKey.FILTER_DISCARD_PREVIOUS));
        discardBtn.setIcon(AppIcon.get("discard", GuiConstant.ICON_SIZE));
        discardBtn.addActionListener(e -> onDiscardClicked());

        resumeBtn = new JButton(MessageUtil.get(MessageKey.TOPBAR_RESUME));
        resumeBtn.setIcon(AppIcon.toolbar("resume"));
        resumeBtn.addActionListener(e -> frame.resume());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttons.add(sinceBtn);
        buttons.add(discardBtn);
        buttons.add(resumeBtn);

        add(infoLabel, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    /** Called by EventBinder on EDT. */
    public void onPauseState(PauseStateEvent event) {
        if (!event.paused()) {
            setVisible(false);
            return;
        }
        setVisible(true);

        String text = MessageUtil.get(MessageKey.PAUSEBAR_PAUSED, event.elapsedText())
                + "  •  "
                + MessageUtil.get(MessageKey.PAUSEBAR_NEW_LINES, event.newLines());
        if (event.hasDeployment()) {
            text = text + "  •  " + MessageUtil.get(MessageKey.PAUSEBAR_DEPLOYMENT_DETECTED,
                    event.deploymentDetectedAt());
            sinceBtn.setVisible(true);
        } else {
            sinceBtn.setVisible(false);
        }
        infoLabel.setText(text);
        revalidate();
    }

    private void onDiscardClicked() {
        if (ConfirmDialog.confirm(frame,
                MessageUtil.get(MessageKey.DIALOG_CONFIRM_DISCARD_TITLE),
                MessageUtil.get(MessageKey.DIALOG_CONFIRM_DISCARD_MSG))) {
            frame.discardPrevious();
        }
    }
}
