package com.maybank.appsense.gui;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.config.SettingsRepository;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.SettingsBean;
import com.maybank.appsense.core.session.LogSessionFactory;
import com.maybank.appsense.gui.service.ThemeService;
import com.maybank.appsense.gui.util.Branding;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

/**
 * Application entry point for the Swing GUI.
 * <p>
 * Startup sequence:
 * <ol>
 *   <li>Read theme early (before ConfigService.init()) so the LAF is set before any Swing component
 *       is instantiated.</li>
 *   <li>Apply the FlatLaf theme.</li>
 *   <li>Show the splash window on the EDT.</li>
 *   <li>Run {@link StartupTask} on a virtual thread (loads config, profiles).</li>
 *   <li>On success: hide splash, open {@link MainFrame}.</li>
 * </ol>
 */
public final class GuiMain {

    private GuiMain() {
    }

    public static void main(String[] args) {
        String theme = readThemeEarly();
        new ThemeService().apply(theme);

        SwingUtilities.invokeLater(() -> {
            Branding branding = new Branding();
            SplashWindow splash = new SplashWindow(branding);
            splash.setVisible(true);

            ConfigService configService = ConfigService.createDefault();
            LogSessionFactory sessionFactory = new LogSessionFactory(configService);

            StartupTask task = new StartupTask(
                    configService,
                    splash,
                    () -> onStartupSuccess(splash, configService, sessionFactory),
                    ex -> onStartupFailure(splash, ex));

            Thread.ofVirtual().name("appsense-startup").start(task);
        });
    }

    private static void onStartupSuccess(SplashWindow splash,
                                          ConfigService configService,
                                          LogSessionFactory sessionFactory) {
        splash.setVisible(false);
        splash.dispose();
        MainFrame frame = new MainFrame(configService, sessionFactory);
        frame.setVisible(true);
    }

    private static void onStartupFailure(SplashWindow splash, Exception ex) {
        splash.setVisible(false);
        splash.dispose();
        JOptionPane.showMessageDialog(null,
                MessageUtil.get(MessageKey.ERROR_CONFIG_CORRUPT, ex.getMessage()),
                MessageUtil.get(MessageKey.DIALOG_ERROR_TITLE),
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /**
     * Reads the theme setting without calling {@code ConfigService.init()}.
     * Falls back to {@link GuiConstant#THEME_DARK} on any error.
     */
    private static String readThemeEarly() {
        try {
            Path home = Path.of(System.getProperty("user.home"), ".appsense");
            SettingsBean settings = new SettingsRepository(home).load();
            return settings.getTheme();
        } catch (Exception ex) {
            return GuiConstant.THEME_DARK;
        }
    }
}
