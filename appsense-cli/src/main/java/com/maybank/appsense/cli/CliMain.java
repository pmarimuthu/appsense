package com.maybank.appsense.cli;

import com.maybank.appsense.cli.command.AppSenseCommand;
import com.maybank.appsense.core.about.AppInfo;
import com.maybank.appsense.core.config.ConfigService;
import com.maybank.appsense.core.config.LoggingBootstrapUtil;
import com.maybank.appsense.core.exception.ConfigException;
import com.maybank.appsense.core.model.AppInfoRecord;
import com.maybank.appsense.core.session.LogSessionFactory;
import picocli.CommandLine;

/**
 * Entry point for the CLI face.
 * Initialises config, builds the picocli command tree, and runs it.
 */
public final class CliMain {

    private CliMain() {}

    public static void main(String[] args) {
        LoggingBootstrapUtil.useBundledConfig();
        ConfigService configService;
        try {
            configService = ConfigService.createDefault();
            configService.init();
        } catch (ConfigException ex) {
            System.err.println("Configuration error: " + ex.getMessage());
            System.exit(1);
            return;
        }
        AppInfoRecord info = buildInfo();
        LogSessionFactory sessionFactory = new LogSessionFactory(configService);
        AppSenseCommand command = new AppSenseCommand(configService, sessionFactory);
        CommandLine cli = new CommandLine(command);
        if (info != null) {
            cli.getCommandSpec().version(
                    info.name() + " " + info.version() + " (build " + info.buildNumber() + ")");
        }
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    private static AppInfoRecord buildInfo() {
        try {
            return new AppInfo().get();
        } catch (ConfigException ex) {
            return null;
        }
    }
}
