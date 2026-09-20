package com.maybank.appsense.gui.dialog;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.LogEntryBean;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sub-panel for managing the list of {@link LogEntryBean} items within {@link ProfileFormDialog}.
 */
public final class LogEntryListPanel extends JPanel {

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final List<LogEntryBean> entries = new ArrayList<>();
    private final JList<String> list;
    private final JButton removeBtn;

    public LogEntryListPanel() {
        super(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder(
                MessageUtil.get(MessageKey.PROFILE_FIELD_LOGS)));

        list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // removeBtn must be fully constructed before the list listener references it
        removeBtn = new JButton(MessageUtil.get(MessageKey.PROFILE_LOG_REMOVE));
        removeBtn.setEnabled(false);
        removeBtn.addActionListener(e -> onRemove());

        list.addListSelectionListener(e -> removeBtn.setEnabled(list.getSelectedIndex() >= 0));

        JButton addBtn = new JButton(MessageUtil.get(MessageKey.PROFILE_LOG_ADD));
        addBtn.addActionListener(e -> onAdd());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.add(addBtn);
        buttons.add(removeBtn);

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public void setEntries(List<LogEntryBean> logEntries) {
        entries.clear();
        entries.addAll(logEntries);
        refreshList();
    }

    public List<LogEntryBean> getEntries() {
        return new ArrayList<>(entries);
    }

    private void refreshList() {
        listModel.clear();
        for (LogEntryBean e : entries) {
            String marker = e.isDeploymentSource() ? " [deploy]" : "";
            listModel.addElement(e.getLabel() + " — " + e.getPath() + marker);
        }
    }

    private void onAdd() {
        JTextField labelField = new JTextField(20);
        JTextField pathField = new JTextField(30);
        JCheckBox deployCheckbox = new JCheckBox(
                MessageUtil.get(MessageKey.PROFILE_LOG_DEPLOY_SOURCE));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 4, 2, 4);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel(MessageUtil.get(MessageKey.PROFILE_LOG_LABEL) + ":"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(labelField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel(MessageUtil.get(MessageKey.PROFILE_LOG_PATH) + ":"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(pathField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        form.add(deployCheckbox, gbc);

        int result = JOptionPane.showConfirmDialog(this, form,
                MessageUtil.get(MessageKey.PROFILE_LOG_LABEL),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            return;
        }
        String label = labelField.getText().trim();
        if (label.isEmpty()) {
            label = path;
        }
        LogEntryBean entry = new LogEntryBean();
        entry.setLabel(label);
        entry.setPath(path);
        entry.setDeploymentSource(deployCheckbox.isSelected());
        entries.add(entry);
        refreshList();
    }

    private void onRemove() {
        int idx = list.getSelectedIndex();
        if (idx >= 0) {
            entries.remove(idx);
            refreshList();
        }
    }
}
