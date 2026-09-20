package com.maybank.appsense.cli.input;

import com.maybank.appsense.cli.CliConstant;
import com.maybank.appsense.cli.screen.FullScreenRenderer;
import com.maybank.appsense.core.model.LevelFilter;
import com.maybank.appsense.core.session.LogSession;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads raw key input from the terminal on a dedicated virtual thread
 * and dispatches actions to the session and renderer.
 */
public final class KeyInputHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(KeyInputHandler.class);
    private static final int READ_TIMEOUT_MS = 100;

    private final Terminal terminal;
    private final FullScreenRenderer renderer;
    private final LogSession session;
    private final Runnable onDiscard;
    private final Runnable onQuit;

    private volatile boolean running = true;
    private boolean searching;
    private boolean sinceDeploy;
    private StringBuilder searchBuf = new StringBuilder();

    public KeyInputHandler(Terminal terminal, FullScreenRenderer renderer,
                           LogSession session, Runnable onDiscard, Runnable onQuit) {
        this.terminal  = terminal;
        this.renderer  = renderer;
        this.session   = session;
        this.onDiscard = onDiscard;
        this.onQuit    = onQuit;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        NonBlockingReader reader = terminal.reader();
        while (running) {
            try {
                int code = reader.read(READ_TIMEOUT_MS);
                if (code < 0) {
                    continue;
                }
                if (searching) {
                    handleSearchKey(code);
                } else {
                    handleNormalKey(code);
                }
            } catch (IOException ex) {
                log.error("Key read error", ex);
                break;
            }
        }
    }

    private void handleNormalKey(int code) {
        if (code == CliConstant.KEY_PAUSE) {
            if (session.isPaused()) session.resume(); else session.pause();
        } else if (code == CliConstant.KEY_SEARCH) {
            searching = true;
            searchBuf.setLength(0);
            renderer.setKeyword("");
        } else if (code == CliConstant.KEY_LEVEL_ALL) {
            session.setLevelFilter(LevelFilter.ALL);
            renderer.setLevelFilter(LevelFilter.ALL);
        } else if (code == CliConstant.KEY_LEVEL_ERROR) {
            session.setLevelFilter(LevelFilter.ERROR);
            renderer.setLevelFilter(LevelFilter.ERROR);
        } else if (code == CliConstant.KEY_LEVEL_WARN) {
            session.setLevelFilter(LevelFilter.WARN);
            renderer.setLevelFilter(LevelFilter.WARN);
        } else if (code == CliConstant.KEY_SINCE_DEPLOY) {
            sinceDeploy = !sinceDeploy;
            session.setSinceLastDeployment(sinceDeploy);
            renderer.redraw();
        } else if (code == CliConstant.KEY_DISCARD) {
            onDiscard.run();
        } else if (code == CliConstant.KEY_MARK) {
            session.markDeployment();
        } else if (code == CliConstant.KEY_COPY) {
            copySelectedSummary();
        } else if (code == CliConstant.KEY_QUIT) {
            onQuit.run();
        } else if (code == CliConstant.KEY_UP) {
            renderer.scrollUp();
        } else if (code == CliConstant.KEY_DOWN) {
            renderer.scrollDown(20);
        } else if (code == CliConstant.KEY_ENTER) {
            renderer.toggleExpand(0);
        }
    }

    private void handleSearchKey(int code) {
        if (code == CliConstant.KEY_ESCAPE) {
            searching = false;
            searchBuf.setLength(0);
            session.setKeywordFilter("");
            renderer.setKeyword("");
        } else if (code == CliConstant.KEY_ENTER) {
            searching = false;
        } else if (code == CliConstant.KEY_BACKSPACE && searchBuf.length() > 0) {
            searchBuf.deleteCharAt(searchBuf.length() - 1);
            String kw = searchBuf.toString();
            session.setKeywordFilter(kw);
            renderer.setKeyword(kw);
        } else if (code >= 32) {
            searchBuf.append((char) code);
            String kw = searchBuf.toString();
            session.setKeywordFilter(kw);
            renderer.setKeyword(kw);
        }
    }

    private void copySelectedSummary() {
        String text = renderer.summaryPane().selectedText();
        if (text.isEmpty()) {
            return;
        }
        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(text), null);
        } catch (Exception ex) {
            log.warn("Clipboard copy failed", ex);
        }
    }
}
