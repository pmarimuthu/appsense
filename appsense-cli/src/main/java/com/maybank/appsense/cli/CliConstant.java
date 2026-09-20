package com.maybank.appsense.cli;

/** Layout and key-binding constants for the CLI face. */
public final class CliConstant {

    public static final int STATS_PANE_WIDTH  = 22;
    public static final int SUMMARY_ROWS      = 6;
    public static final int LOG_TIME_WIDTH    = 12;
    public static final int LOG_LEVEL_WIDTH   = 6;
    public static final int LOG_LOGGER_WIDTH  = 25;
    public static final int SPARKLINE_BARS    = 10;

    public static final char KEY_PAUSE        = ' ';
    public static final char KEY_SEARCH       = '/';
    public static final char KEY_LEVEL_ALL    = 'a';
    public static final char KEY_LEVEL_ERROR  = 'e';
    public static final char KEY_LEVEL_WARN   = 'w';
    public static final char KEY_SINCE_DEPLOY = 'd';
    public static final char KEY_DISCARD      = 'x';
    public static final char KEY_MARK         = 'm';
    public static final char KEY_COPY         = 'c';
    public static final char KEY_QUIT         = 'q';
    public static final char KEY_CONFIRM_YES  = 'y';
    public static final char KEY_CONFIRM_NO   = 'n';
    public static final int  KEY_ESCAPE       = 27;
    public static final int  KEY_ENTER        = 13;
    public static final int  KEY_BACKSPACE    = 127;
    public static final int  KEY_UP           = -1001;
    public static final int  KEY_DOWN         = -1002;

    public static final String SEPARATOR      = "─";
    public static final String PIPE           = "│";
    public static final String TEE_DOWN       = "┬";
    public static final String TEE_UP         = "┴";
    public static final String BULLET_LIVE    = "●";
    public static final String BULLET_PAUSE   = "⏸";

    public static final String STATUS_LIVE    = "Live";
    public static final String STATUS_PAUSED  = "Paused";
    public static final String STATUS_RECONN  = "Reconnecting…";
    public static final String STATUS_DISCONN = "Disconnected";

    private CliConstant() {}
}
