package com.maybank.appsense.core.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

public final class SshConstant {

    public static final int DEFAULT_PORT = 22;
    public static final int KEEPALIVE_INTERVAL_SECONDS = 30;
    public static final Duration HEARTBEAT_REPLY_WAIT = Duration.ofSeconds(15);
    public static final int CONNECT_TIMEOUT_SECONDS = 15;
    public static final int AUTH_TIMEOUT_SECONDS = 20;
    public static final int COMMAND_TIMEOUT_SECONDS = 30;
    public static final long WAIT_FOREVER_MS = 0L;
    public static final Duration IDLE_TIMEOUT_DISABLED = Duration.ZERO;

    public static final int MAX_RECONNECT_ATTEMPTS = 8;
    public static final long RECONNECT_INITIAL_DELAY_MS = 1000L;
    public static final long RECONNECT_MAX_DELAY_MS = 30000L;
    public static final long RECONNECT_BACKOFF_FACTOR = 2L;

    public static final Charset REMOTE_CHARSET = StandardCharsets.UTF_8;
    public static final byte LINE_FEED = '\n';
    public static final char CARRIAGE_RETURN = '\r';

    public static final String SHELL_QUOTE = "'";
    public static final String SHELL_QUOTE_ESCAPED = "'\\''";
    public static final String STAT_COMMAND = "LC_ALL=C stat -c '%%i %%s' -- %s";
    public static final String TAIL_FROM_OFFSET_COMMAND = "LC_ALL=C tail -c +%d -F -- %s 2>&1";
    public static final String TAIL_NOTICE_PREFIX = "tail: ";
    public static final String TAIL_LAST_LINES_COMMAND = "LC_ALL=C tail -n %d -- %s";
    public static final String STAT_FIELD_SEPARATOR = "\\s+";
    public static final int STAT_FIELD_COUNT = 2;
    public static final int STAT_INODE_INDEX = 0;
    public static final int STAT_SIZE_INDEX = 1;

    public static final long INITIAL_BACKLOG_BYTES = 2_097_152L;
    public static final int DEPLOY_SCAN_LINES = 10_000;

    public static final List<String> ROTATION_NOTICE_MARKERS = List.of(
            "has been replaced", "file truncated", "has appeared");

    private SshConstant() {
    }
}
