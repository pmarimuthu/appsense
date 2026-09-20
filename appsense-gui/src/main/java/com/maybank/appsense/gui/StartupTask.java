package com.maybank.appsense.gui;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;

import javax.swing.SwingUtilities;
import java.util.function.Consumer;

/**
 * Runs {@link ConfigService#init()} on a background thread, reporting progress to the
 * splash screen via EDT callbacks, then notifies success or failure.
 * Enforces a minimum splash display time so the screen is always visible briefly.
 */
public final class StartupTask implements Runnable {

    private final ConfigService configService;
    private final SplashWindow splash;
    private final Runnable onSuccess;
    private final Consumer<Exception> onFailure;

    public StartupTask(ConfigService configService,
                       SplashWindow splash,
                       Runnable onSuccess,
                       Consumer<Exception> onFailure) {
        this.configService = configService;
        this.splash = splash;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            step(MessageUtil.get(MessageKey.SPLASH_LOADING_CONFIG));
            configService.init();
            step(MessageUtil.get(MessageKey.SPLASH_LOADING_PROFILES));
            configService.getProfiles();
            step(MessageUtil.get(MessageKey.SPLASH_BUILDING_UI));
            sleepRemaining(start);
            SwingUtilities.invokeLater(onSuccess);
        } catch (ConfigException ex) {
            SwingUtilities.invokeLater(() -> onFailure.accept(ex));
        }
    }

    private void step(String text) {
        SwingUtilities.invokeLater(() -> splash.setStep(text));
    }

    private void sleepRemaining(long start) {
        long remaining = GuiConstant.SPLASH_MIN_DISPLAY_MS - (System.currentTimeMillis() - start);
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
