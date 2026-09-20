package com.maybank.appsense.gui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads branding text (app name, tagline, version) from {@code branding.properties}.
 * Falls back to hardcoded defaults if the resource is missing.
 */
public final class Branding {

    private static final String RESOURCE = "/com/maybank/appsense/gui/branding.properties";

    private final String appName;
    private final String tagline;
    private final String version;

    public Branding() {
        Properties props = new Properties();
        try (InputStream in = Branding.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
        }
        appName = props.getProperty("app.name", "AppSense");
        tagline = props.getProperty("app.tagline", "Log viewer for JBoss and WebLogic");
        version = props.getProperty("app.version", "");
    }

    public String getAppName() {
        return appName;
    }

    public String getTagline() {
        return tagline;
    }

    public String getVersion() {
        return version;
    }
}
