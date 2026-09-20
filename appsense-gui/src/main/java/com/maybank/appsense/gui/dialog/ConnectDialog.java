package com.maybank.appsense.gui.dialog;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.gui.GuiConstant;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Modal dialog that collects the SSH password for a profile.
 * Returns {@code null} if the user cancels.
 * The returned {@code char[]} must be wiped by the caller after use.
 */
public final class ConnectDialog {

    private ConnectDialog() {
    }

    /**
     * Shows the dialog and returns the entered password, or {@code null} on cancel.
     */
    public static char[] ask(Frame parent, ProfileBean profile) {
        JPasswordField pwField = new JPasswordField(24);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel(
                MessageUtil.get(MessageKey.DIALOG_CONNECT_PASSWORD,
                        profile.getUsername(), profile.getHost())), gbc);

        gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pwField, gbc);

        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(MessageUtil.get(MessageKey.CONNECT_PASSWORD_HINT)), gbc);

        String title = MessageUtil.get(MessageKey.DIALOG_CONNECT_TITLE, profile.getName());
        int result = JOptionPane.showConfirmDialog(
                parent, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }
        return pwField.getPassword();
    }
}
