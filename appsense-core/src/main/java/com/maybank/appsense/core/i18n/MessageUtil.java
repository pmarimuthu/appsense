package com.maybank.appsense.core.i18n;

import com.maybank.appsense.core.config.AppConstant;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class MessageUtil {

    private static volatile ResourceBundle bundle;

    private MessageUtil() {
    }

    /**
     * Loads the message bundle for the given locale. Must be called before any face renders text;
     * if it is not, the first {@code get} call loads the default locale.
     */
    public static synchronized void init(Locale locale) {
        bundle = ResourceBundle.getBundle(AppConstant.MESSAGES_BUNDLE, locale);
    }

    /** Returns the message text for the key. Throws {@code MissingResourceException} if absent. */
    public static String get(MessageKey key) {
        return current().getString(key.getKey());
    }

    /** Returns the message text for the key with {@link MessageFormat} arguments applied. */
    public static String get(MessageKey key, Object... args) {
        return MessageFormat.format(current().getString(key.getKey()), args);
    }

    private static ResourceBundle current() {
        ResourceBundle loaded = bundle;
        if (loaded == null) {
            init(Locale.getDefault());
            loaded = bundle;
        }
        return loaded;
    }
}
