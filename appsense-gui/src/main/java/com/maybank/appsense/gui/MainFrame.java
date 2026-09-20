package com.maybank.appsense.gui;

import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.i18n.MessageKey;
import com.maybank.appsense.core.i18n.MessageUtil;
import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.session.LogSession;
import com.maybank.appsense.core.session.LogSessionFactory;
import com.maybank.appsense.gui.dialog.ConnectDialog;
import com.maybank.appsense.gui.dialog.ProfileFormDialog;
import com.maybank.appsense.gui.panel.FilterBar;
import com.maybank.appsense.gui.panel.LogPanel;
import com.maybank.appsense.gui.panel.PauseBar;
import com.maybank.appsense.gui.panel.StatsPanel;
import com.maybank.appsense.gui.panel.StatusBar;
import com.maybank.appsense.gui.panel.SummaryPanel;
import com.maybank.appsense.gui.panel.TopBar;
import com.maybank.appsense.gui.util.AppIcon;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window. Holds all panels and orchestrates session lifecycle.
 */
public final class MainFrame extends JFrame {

    private final ConfigService configService;
    private final LogSessionFactory sessionFactory;
    private LogSession activeSession;

    private final TopBar topBar;
    private final PauseBar pauseBar;
    private final FilterBar filterBar;
    private final StatusBar statusBar;
    private final LogPanel logPanel;
    private final StatsPanel statsPanel;
    private final SummaryPanel summaryPanel;
    private final EventBinder eventBinder;

    public MainFrame(ConfigService configService, LogSessionFactory sessionFactory) {
        this.configService = configService;
        this.sessionFactory = sessionFactory;

        setTitle(MessageUtil.get(MessageKey.APP_NAME));
        setIconImage(AppIcon.appImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(GuiConstant.MAIN_MIN_WIDTH, GuiConstant.MAIN_MIN_HEIGHT));

        logPanel = new LogPanel();
        statsPanel = new StatsPanel();
        summaryPanel = new SummaryPanel();
        topBar = new TopBar(this);
        pauseBar = new PauseBar(this);
        filterBar = new FilterBar(this);
        statusBar = new StatusBar();
        eventBinder = new EventBinder(topBar, pauseBar, filterBar, statusBar,
                logPanel, statsPanel, summaryPanel);

        buildLayout();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }

    private void buildLayout() {
        JPanel north = new JPanel(new BorderLayout());
        north.add(topBar, BorderLayout.NORTH);
        north.add(pauseBar, BorderLayout.CENTER);
        north.add(filterBar, BorderLayout.SOUTH);

        JPanel rightPane = new JPanel(new BorderLayout());
        rightPane.setPreferredSize(new Dimension(GuiConstant.STATS_PANEL_WIDTH, 0));
        rightPane.add(statsPanel, BorderLayout.NORTH);
        rightPane.add(summaryPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, rightPane);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(4);

        JPanel root = new JPanel(new BorderLayout());
        root.add(north, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Opens a new session with the given profile. Password dialog shown if needed. */
    public void connect(ProfileBean profile, LogEntryBean logEntry) {
        char[] password = ConnectDialog.ask(this, profile);
        if (password == null) {
            return;
        }
        closeActiveSession();
        LogSession session = sessionFactory.create();
        eventBinder.bind(session.getEventBus());
        activeSession = session;
        topBar.setProfileAndLog(profile.getName(), logEntry.getLabel());
        Thread.ofVirtual().name("appsense-connect").start(() -> {
            try {
                session.connect(profile, password, logEntry);
            } catch (AppSenseException ex) {
                SwingUtilities.invokeLater(() -> showError(ex.getMessage()));
            }
        });
    }

    /** Disconnects the current session. */
    public void disconnect() {
        closeActiveSession();
    }

    /** Shows the profile manager dialog. */
    public void openProfileManager() {
        ProfileFormDialog.show(this, configService);
    }

    public void pause() {
        if (activeSession != null) {
            activeSession.pause();
        }
    }

    public void resume() {
        if (activeSession != null) {
            activeSession.resume();
        }
    }

    public void discardPrevious() {
        if (activeSession != null) {
            activeSession.discardPrevious();
        }
    }

    public void markDeployment() {
        if (activeSession != null) {
            activeSession.markDeployment();
        }
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public LogSession getActiveSession() {
        return activeSession;
    }

    private void closeActiveSession() {
        if (activeSession != null) {
            activeSession.close();
            activeSession = null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                MessageUtil.get(MessageKey.DIALOG_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
    }

    private void shutdown() {
        closeActiveSession();
        try {
            configService.saveSettings();
        } catch (ConfigException ignored) {
        }
        dispose();
        System.exit(0);
    }
}
