package com.maybank.appsense.cli.command;

import com.maybank.appsense.core.model.LogEntryBean;
import com.maybank.appsense.core.model.ProfileBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

/** Interactive numbered-list picker for profiles and log entries. */
public final class ProfilePickerUtil {

    private final Terminal terminal;
    private final PrintWriter out;

    public ProfilePickerUtil(Terminal terminal) {
        this.terminal = terminal;
        this.out      = terminal.writer();
    }

    /**
     * Asks the user to pick a profile from the list.
     *
     * @return the chosen profile, or the first if the list has exactly one entry
     * @throws IllegalStateException when the list is empty
     */
    public ProfileBean pickProfile(List<ProfileBean> profiles) {
        if (profiles.isEmpty()) {
            throw new IllegalStateException("No profiles configured. Run the GUI first to add one.");
        }
        if (profiles.size() == 1) {
            return profiles.get(0);
        }
        out.println("Available profiles:");
        for (int i = 0; i < profiles.size(); i++) {
            out.println("  " + (i + 1) + ") " + profiles.get(i).getName());
        }
        int idx = readInt("Select profile [1]: ", 1, profiles.size(), 1);
        return profiles.get(idx - 1);
    }

    /**
     * Asks the user to pick a log entry from a profile.
     *
     * @return the chosen log entry, or the first if the list has exactly one entry
     * @throws IllegalStateException when the profile has no log entries
     */
    public LogEntryBean pickLog(ProfileBean profile) {
        List<LogEntryBean> logs = profile.getLogs();
        if (logs.isEmpty()) {
            throw new IllegalStateException("Profile '" + profile.getName() + "' has no log entries.");
        }
        if (logs.size() == 1) {
            return logs.get(0);
        }
        out.println("Available logs in '" + profile.getName() + "':");
        for (int i = 0; i < logs.size(); i++) {
            out.println("  " + (i + 1) + ") " + logs.get(i).getLabel());
        }
        int idx = readInt("Select log [1]: ", 1, logs.size(), 1);
        return logs.get(idx - 1);
    }

    private int readInt(String prompt, int min, int max, int defaultVal) {
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
        while (true) {
            String line = reader.readLine(prompt);
            if (line == null || line.isBlank()) {
                return defaultVal;
            }
            try {
                int v = Integer.parseInt(line.trim());
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException ignored) {
            }
            out.println("  Please enter a number between " + min + " and " + max + ".");
        }
    }
}
