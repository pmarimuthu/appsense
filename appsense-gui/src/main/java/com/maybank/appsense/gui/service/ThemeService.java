package com.maybank.appsense.gui.service;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.maybank.appsense.gui.GuiConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;

/** Applies FlatLaf look-and-feel by theme name. */
public final class ThemeService {

    private static final Logger LOG = LoggerFactory.getLogger(ThemeService.class);

    public void apply(String theme) {
        try {
            if (GuiConstant.THEME_LIGHT.equalsIgnoreCase(theme)) {
                FlatIntelliJLaf.setup();
            } else {
                FlatDarculaLaf.setup();
            }
        } catch (Exception ex) {
            LOG.warn("Failed to apply theme '{}', falling back to default", theme, ex);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }
}
