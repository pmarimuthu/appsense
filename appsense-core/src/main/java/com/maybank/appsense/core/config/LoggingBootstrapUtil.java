package com.maybank.appsense.core.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import com.maybank.appsense.core.model.SettingsBean;
import java.nio.file.Path;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingBootstrapUtil {

    private LoggingBootstrapUtil() {
    }

    /** Points logback at the bundled configuration. Call before the first logger is created. */
    public static void useBundledConfig() {
        System.setProperty(AppConstant.LOGBACK_CONFIG_PROPERTY, AppConstant.LOGBACK_CONFIG_RESOURCE);
    }

    /**
     * Publishes the log folder and rotation settings as system properties for logback.xml and, when
     * logback is already running, reloads its configuration so the values take effect.
     */
    public static void apply(Path homeDir, SettingsBean settings) {
        System.setProperty(AppConstant.LOG_DIR_PROPERTY, homeDir.resolve(AppConstant.LOG_FOLDER).toString());
        System.setProperty(AppConstant.LOG_FILE_PROPERTY, AppConstant.APP_LOG_FILE);
        System.setProperty(AppConstant.LOG_ROTATE_SIZE_PROPERTY, String.valueOf(settings.getAppLogRotateSizeMb()));
        System.setProperty(AppConstant.LOG_MAX_HISTORY_PROPERTY, String.valueOf(settings.getAppLogMaxHistory()));
        reload();
    }

    private static void reload() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (factory instanceof LoggerContext context) {
            context.reset();
            try {
                new ContextInitializer(context).autoConfig();
            } catch (JoranException ex) {
                Logger log = LoggerFactory.getLogger(LoggingBootstrapUtil.class);
                log.error("Logging configuration could not be reloaded", ex);
            }
        }
    }
}
