package com.maybank.appsense.core.config;

public final class AppConstant {

    public static final String HOME_FOLDER_NAME = ".appsense";
    public static final String PROFILES_FILE = "profiles.json";
    public static final String PROFILES_BAK = "profiles.json.bak";
    public static final String SETTINGS_FILE = "settings.json";
    public static final String SETTINGS_BAK = "settings.json.bak";
    public static final String TMP_SUFFIX = ".tmp";
    public static final String LOG_FOLDER = "logs";
    public static final String APP_LOG_FILE = "appsense.log";

    public static final String USER_HOME_PROPERTY = "user.home";
    public static final String JAVA_VERSION_PROPERTY = "java.version";
    public static final String OS_NAME_PROPERTY = "os.name";

    public static final String LOG_DIR_PROPERTY = "appsense.log.dir";
    public static final String LOG_FILE_PROPERTY = "appsense.log.file";
    public static final String LOG_ROTATE_SIZE_PROPERTY = "appsense.log.rotateSizeMb";
    public static final String LOG_MAX_HISTORY_PROPERTY = "appsense.log.maxHistory";
    public static final String LOGBACK_CONFIG_PROPERTY = "logback.configurationFile";
    public static final String LOGBACK_CONFIG_RESOURCE = "com/maybank/appsense/core/logback.xml";

    public static final String MESSAGES_BUNDLE = "com.maybank.appsense.core.i18n.messages";
    public static final String DEFAULT_SETTINGS_RESOURCE = "/com/maybank/appsense/core/default-settings.properties";
    public static final String DEFAULT_MARKERS_RESOURCE = "/com/maybank/appsense/core/default-markers.properties";
    public static final String BUILD_INFO_RESOURCE = "/com/maybank/appsense/core/build-info.properties";
    public static final String BUILD_VERSION_KEY = "app.version";
    public static final String BUILD_NUMBER_KEY = "app.buildNumber";
    public static final String BUILD_DATE_KEY = "app.buildDate";

    public static final String EVENT_BUS_THREAD = "appsense-event-bus";
    public static final String BATCH_THREAD = "appsense-batch";
    public static final String TAIL_THREAD = "appsense-tail";
    public static final String WATCH_THREAD = "appsense-deploy-watch";
    public static final String RECONNECT_THREAD = "appsense-reconnect";

    public static final int JSON_SCHEMA_VERSION = 1;
    public static final int EXIT_CODE_ERROR = 1;
    public static final int NOT_FOUND = -1;
    public static final char NUL_CHAR = '\0';
    public static final String ELAPSED_FORMAT = "%02d:%02d:%02d";
    public static final String SUMMARY_KEY_SEPARATOR = "\u0001";

    private AppConstant() {
    }
}
