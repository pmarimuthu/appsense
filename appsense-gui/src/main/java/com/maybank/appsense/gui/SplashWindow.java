package com.maybank.appsense.gui;

import com.maybank.appsense.gui.util.Branding;
import com.maybank.appsense.gui.util.AppIcon;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Undecorated splash screen shown during startup.
 * Call {@link #setStep(String)} from the EDT to update the status text.
 */
public final class SplashWindow extends JWindow {

    private final JLabel statusLabel;

    public SplashWindow(Branding branding) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(0x1e1f22));
        root.setBorder(BorderFactory.createLineBorder(new Color(0x3c3f41), 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 24, 0, 24);

        JLabel nameLabel = new JLabel(branding.getAppName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        nameLabel.setForeground(new Color(0xfdb50a));
        gbc.gridy = 0;
        gbc.insets = new Insets(32, 24, 4, 24);
        root.add(nameLabel, gbc);

        JLabel tagLabel = new JLabel(branding.getTagline(), SwingConstants.CENTER);
        tagLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tagLabel.setForeground(new Color(0x9da0a5));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 24, 20, 24);
        root.add(tagLabel, gbc);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(new Color(0x6b6f76));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 24, 24, 24);
        root.add(statusLabel, gbc);

        setContentPane(root);
        setPreferredSize(new Dimension(GuiConstant.SPLASH_MIN_WIDTH, GuiConstant.SPLASH_MIN_HEIGHT));
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    /** Updates the step text. Must be called on the EDT. */
    public void setStep(String text) {
        statusLabel.setText(text);
    }
}
