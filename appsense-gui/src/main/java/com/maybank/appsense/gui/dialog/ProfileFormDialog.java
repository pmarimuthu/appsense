package com.maybank.appsense.gui.dialog;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.model.ServerType;
import com.maybank.appsense.gui.GuiConstant;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Modal dialog for creating and editing connection profiles.
 * Opened via {@link #show(Frame, ConfigService)}.
 */
public final class ProfileFormDialog extends JDialog {

    private final ConfigService configService;
    private final DefaultListModel<String> profileListModel = new DefaultListModel<>();
    private final JList<String> profileList;
    private List<ProfileBean> profiles;

    private ProfileFormDialog(Frame parent, ConfigService configService) {
        super(parent, MessageUtil.get(MessageKey.DIALOG_PROFILE_ADD_TITLE), true);
        this.configService = configService;

        profiles = configService.getProfiles();
        profileList = new JList<>(profileListModel);
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshList();

        JButton addBtn = new JButton(MessageUtil.get(MessageKey.PROFILE_LOG_ADD));
        addBtn.addActionListener(e -> onAdd());

        JButton editBtn = new JButton(MessageUtil.get(MessageKey.DIALOG_PROFILE_EDIT_TITLE));
        editBtn.addActionListener(e -> onEdit());

        JButton deleteBtn = new JButton(MessageUtil.get(MessageKey.PROFILE_LOG_REMOVE));
        deleteBtn.addActionListener(e -> onDelete());

        JButton closeBtn = new JButton(MessageUtil.get(MessageKey.DIALOG_OK));
        closeBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(closeBtn);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.add(new JScrollPane(profileList), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.NORTH);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(GuiConstant.DIALOG_PROFILE_WIDTH, GuiConstant.DIALOG_PROFILE_HEIGHT);
        setLocationRelativeTo(parent);
    }

    public static void show(Frame parent, ConfigService configService) {
        new ProfileFormDialog(parent, configService).setVisible(true);
    }

    private void refreshList() {
        profileListModel.clear();
        for (ProfileBean p : profiles) {
            profileListModel.addElement(p.getName());
        }
    }

    private void onAdd() {
        ProfileBean profile = showForm(null);
        if (profile == null) {
            return;
        }
        trySave(profile);
    }

    private void onEdit() {
        int idx = profileList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        ProfileBean profile = showForm(profiles.get(idx));
        if (profile == null) {
            return;
        }
        trySave(profile);
    }

    private void onDelete() {
        int idx = profileList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        String name = profiles.get(idx).getName();
        if (!ConfirmDialog.confirm(this,
                MessageUtil.get(MessageKey.DIALOG_CONFIRM_DISCARD_TITLE),
                MessageUtil.get(MessageKey.DIALOG_PROFILE_DELETE_CONFIRM, name))) {
            return;
        }
        try {
            configService.deleteProfile(name);
            profiles = configService.getProfiles();
            refreshList();
        } catch (ConfigException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    MessageUtil.get(MessageKey.DIALOG_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void trySave(ProfileBean profile) {
        try {
            configService.saveProfile(profile);
            profiles = configService.getProfiles();
            refreshList();
        } catch (ConfigException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    MessageUtil.get(MessageKey.DIALOG_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
        }
    }

    private ProfileBean showForm(ProfileBean existing) {
        boolean isEdit = existing != null;
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "", 20);
        JTextField hostField = new JTextField(existing != null ? existing.getHost() : "", 20);
        JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(
                existing != null ? existing.getPort() : 22, 1, 65535, 1));
        JTextField userField = new JTextField(existing != null ? existing.getUsername() : "", 20);
        JComboBox<ServerType> typeBox = new JComboBox<>(ServerType.values());
        if (existing != null) {
            typeBox.setSelectedItem(existing.getServerType());
        }
        JTextField packagesField = new JTextField(
                existing != null ? String.join(",", existing.getAppPackages()) : "", 30);
        LogEntryListPanel logPanel = new LogEntryListPanel();
        if (existing != null) {
            logPanel.setEntries(existing.getLogs());
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 4, 3, 4);

        int row = 0;
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_NAME), nameField);
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_HOST), hostField);
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_PORT), portSpinner);
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_USERNAME), userField);
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_SERVER_TYPE), typeBox);
        addRow(form, gbc, row++, MessageUtil.get(MessageKey.PROFILE_FIELD_APP_PACKAGES), packagesField);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1;
        form.add(logPanel, gbc);

        String title = isEdit
                ? MessageUtil.get(MessageKey.DIALOG_PROFILE_EDIT_TITLE)
                : MessageUtil.get(MessageKey.DIALOG_PROFILE_ADD_TITLE);
        int result = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        ProfileBean p = isEdit ? existing : new ProfileBean();
        p.setName(nameField.getText().trim());
        p.setHost(hostField.getText().trim());
        p.setPort((Integer) portSpinner.getValue());
        p.setUsername(userField.getText().trim());
        p.setServerType((ServerType) typeBox.getSelectedItem());
        String pkgs = packagesField.getText().trim();
        p.setAppPackages(pkgs.isEmpty() ? List.of()
                : java.util.Arrays.stream(pkgs.split(",")).map(String::trim).collect(java.util.stream.Collectors.toList()));
        p.setLogs(logPanel.getEntries());
        return p;
    }

    private static void addRow(JPanel form, GridBagConstraints gbc, int row,
                                String label, java.awt.Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        form.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(field, gbc);
    }
}
