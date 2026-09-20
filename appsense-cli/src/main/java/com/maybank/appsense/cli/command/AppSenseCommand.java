package com.maybank.appsense.cli.command;

import com.maybank.appsense.cli.bind.EventBinder;
import com.maybank.appsense.cli.input.KeyInputHandler;
import com.maybank.appsense.cli.input.PasswordPromptUtil;
import com.maybank.appsense.cli.screen.FullScreenRenderer;
import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.session.LogSession;
import com.maybank.appsense.core.session.LogSessionFactory;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * picocli root command. Wires terminal, renderer, session, and key handler.
 */
@Command(name = "appsense-cli", mixinStandardHelpOptions = true,
        description = "Live log viewer for JBoss EAP and WebLogic.")
public final class AppSenseCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Profile name")
    private String profileName;

    @Parameters(index = "1", arity = "0..1", description = "Log entry label")
    private String logLabel;

    private final ConfigService configService;
    private final LogSessionFactory sessionFactory;

    public AppSenseCommand(ConfigService configService, LogSessionFactory sessionFactory) {
        this.configService  = configService;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("AppSense error: " + ex.getMessage());
        }
    }

    private void execute() throws Exception {
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        PrintWriter out = terminal.writer();
        FullScreenRenderer renderer = new FullScreenRenderer(terminal);
        EventBinder binder = new EventBinder(renderer);
        ProfilePickerUtil picker = new ProfilePickerUtil(terminal);
        PasswordPromptUtil passwordUtil = new PasswordPromptUtil(terminal);

        List<ProfileBean> profiles = configService.getProfiles();
        ProfileBean profile = resolveProfile(profiles, picker);
        LogEntryBean logEntry = resolveLog(profile, picker);
        renderer.setProfile(profile.getName(), logEntry.getLabel());

        LogSession session = sessionFactory.create();
        binder.bind(session.getEventBus());

        CountDownLatch quitLatch = new CountDownLatch(1);
        KeyInputHandler keyHandler = new KeyInputHandler(
                terminal, renderer, session,
                () -> confirmDiscard(session, terminal, out),
                quitLatch::countDown);

        char[] password = passwordUtil.prompt(
                "Password for " + profile.getUsername() + "@" + profile.getHost() + ": ");
        try {
            session.connect(profile, password, logEntry);
        } catch (AppSenseException ex) {
            out.println("Connection failed: " + ex.getMessage());
            out.flush();
            session.close();
            terminal.close();
            return;
        }

        renderer.redraw();
        Thread.ofVirtual().name("appsense-keys").start(keyHandler);
        quitLatch.await();
        keyHandler.stop();
        session.close();
        renderer.close();
        terminal.close();
    }

    private ProfileBean resolveProfile(List<ProfileBean> profiles, ProfilePickerUtil picker) {
        if (profileName != null) {
            return profiles.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(profileName))
                    .findFirst()
                    .orElseGet(() -> picker.pickProfile(profiles));
        }
        return picker.pickProfile(profiles);
    }

    private LogEntryBean resolveLog(ProfileBean profile, ProfilePickerUtil picker) {
        if (logLabel != null) {
            return profile.findLog(logLabel)
                    .orElseGet(() -> picker.pickLog(profile));
        }
        return picker.pickLog(profile);
    }

    private void confirmDiscard(LogSession session, Terminal terminal, PrintWriter out) {
        out.print("Discard previous log lines? [y/n]: ");
        out.flush();
        try {
            int ch = terminal.reader().read(5000);
            if (ch == 'y' || ch == 'Y') {
                session.discardPrevious();
            }
        } catch (Exception ignored) {
        }
    }
}
