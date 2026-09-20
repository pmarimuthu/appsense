package com.maybank.appsense.core.model;

public class SettingsBean {

    private int version;
    private String language;
    private String theme;
    private int splashSeconds;
    private int keepAliveIntervalSeconds;
    private int pauseBufferCap;
    private int uiBatchIntervalMs;
    private int appLogRotateSizeMb;
    private int appLogMaxHistory;
    private boolean autoDiscardOnDeployment;
    private String lastUsedProfile;
    private String lastUsedLog;

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getSplashSeconds() { return splashSeconds; }
    public void setSplashSeconds(int splashSeconds) { this.splashSeconds = splashSeconds; }

    public int getKeepAliveIntervalSeconds() { return keepAliveIntervalSeconds; }
    public void setKeepAliveIntervalSeconds(int keepAliveIntervalSeconds) {
        this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;
    }

    public int getPauseBufferCap() { return pauseBufferCap; }
    public void setPauseBufferCap(int pauseBufferCap) { this.pauseBufferCap = pauseBufferCap; }

    public int getUiBatchIntervalMs() { return uiBatchIntervalMs; }
    public void setUiBatchIntervalMs(int uiBatchIntervalMs) { this.uiBatchIntervalMs = uiBatchIntervalMs; }

    public int getAppLogRotateSizeMb() { return appLogRotateSizeMb; }
    public void setAppLogRotateSizeMb(int appLogRotateSizeMb) { this.appLogRotateSizeMb = appLogRotateSizeMb; }

    public int getAppLogMaxHistory() { return appLogMaxHistory; }
    public void setAppLogMaxHistory(int appLogMaxHistory) { this.appLogMaxHistory = appLogMaxHistory; }

    public boolean isAutoDiscardOnDeployment() { return autoDiscardOnDeployment; }
    public void setAutoDiscardOnDeployment(boolean autoDiscardOnDeployment) {
        this.autoDiscardOnDeployment = autoDiscardOnDeployment;
    }

    public String getLastUsedProfile() { return lastUsedProfile; }
    public void setLastUsedProfile(String lastUsedProfile) { this.lastUsedProfile = lastUsedProfile; }

    public String getLastUsedLog() { return lastUsedLog; }
    public void setLastUsedLog(String lastUsedLog) { this.lastUsedLog = lastUsedLog; }
}
