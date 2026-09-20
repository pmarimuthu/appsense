package com.maybank.appsense.gui.panel;

import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.gui.GuiConstant;
import com.maybank.appsense.gui.MainFrame;
import com.maybank.appsense.gui.util.AppIcon;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.FlowLayout;

/**
 * Filter controls bar: level toggle buttons and keyword search field.
 */
public final class FilterBar extends JPanel {

    private final MainFrame frame;
    private final JTextField searchField;
    private final JToggleButton allBtn;
    private final JToggleButton warnBtn;
    private final JToggleButton errorBtn;
    private final JButton markBtn;

    public FilterBar(MainFrame frame) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 2));
        this.frame = frame;
        setPreferredSize(new java.awt.Dimension(0, GuiConstant.FILTERBAR_HEIGHT));

        allBtn = new JToggleButton(MessageUtil.get(MessageKey.FILTER_LEVEL_ALL));
        allBtn.setSelected(true);
        warnBtn = new JToggleButton(MessageUtil.get(MessageKey.FILTER_LEVEL_WARN));
        errorBtn = new JToggleButton(MessageUtil.get(MessageKey.FILTER_LEVEL_ERROR));

        ButtonGroup group = new ButtonGroup();
        group.add(allBtn);
        group.add(warnBtn);
        group.add(errorBtn);

        allBtn.addActionListener(e -> applyLevelFilter(LevelFilter.ALL));
        warnBtn.addActionListener(e -> applyLevelFilter(LevelFilter.WARN));
        errorBtn.addActionListener(e -> applyLevelFilter(LevelFilter.ERROR));

        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText",
                MessageUtil.get(MessageKey.FILTER_SEARCH_PLACEHOLDER));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyKeyword(); }
            public void removeUpdate(DocumentEvent e) { applyKeyword(); }
            public void changedUpdate(DocumentEvent e) { applyKeyword(); }
        });

        markBtn = new JButton(MessageUtil.get(MessageKey.FILTER_MARK_DEPLOYMENT));
        markBtn.setIcon(AppIcon.get("mark", GuiConstant.ICON_SIZE));
        markBtn.addActionListener(e -> frame.markDeployment());

        add(allBtn);
        add(warnBtn);
        add(errorBtn);
        add(searchField);
        add(markBtn);
    }

    private void applyLevelFilter(LevelFilter filter) {
        if (frame.getActiveSession() != null) {
            frame.getActiveSession().setLevelFilter(filter);
        }
    }

    private void applyKeyword() {
        if (frame.getActiveSession() != null) {
            frame.getActiveSession().setKeywordFilter(searchField.getText());
        }
    }
}
