package com.maybank.appsense.gui.dialog;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Reusable yes/no confirmation dialog.
 */
public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    /**
     * Shows a yes/no dialog and returns {@code true} if the user clicked Yes.
     */
    public static boolean confirm(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
}
