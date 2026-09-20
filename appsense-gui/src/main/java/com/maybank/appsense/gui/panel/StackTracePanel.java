package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.model.LogLineRecord;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Sub-panel inside {@link LogPanel} that shows the stack trace of the selected log line.
 * Visible only when an error row with a non-empty stack trace is selected.
 */
public final class StackTracePanel extends JPanel {

    private final JTextArea textArea;

    public StackTracePanel() {
        super(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
        textArea.setLineWrap(false);

        add(new JScrollPane(textArea), BorderLayout.CENTER);
        setPreferredSize(new java.awt.Dimension(0,
                com.maybank.appsense.gui.GuiConstant.STACK_PANEL_HEIGHT));
        setVisible(false);
    }

    /**
     * Shows the panel with the given record's stack trace.
     * Pass {@code null} or a record with an empty trace to hide.
     */
    public void show(LogLineRecord record) {
        if (record == null || record.stackTrace().isEmpty()) {
            setVisible(false);
            textArea.setText("");
            return;
        }
        List<String> lines = record.stackTrace();
        textArea.setText(String.join("\n", lines));
        textArea.setCaretPosition(0);
        setVisible(true);
    }
}
