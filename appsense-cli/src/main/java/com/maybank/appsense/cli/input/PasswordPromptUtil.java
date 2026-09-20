package com.maybank.appsense.cli.input;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;

/** Prompts for a password via JLine, masking input with '*'. */
public final class PasswordPromptUtil {

    private static final char MASK = '*';

    private final Terminal terminal;

    public PasswordPromptUtil(Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     * Blocks until the user enters a password.
     *
     * @param prompt text to display before the input field
     * @return password as char[], never null; empty if the user just pressed Enter
     */
    public char[] prompt(String prompt) {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        String line = reader.readLine(prompt, MASK);
        return line == null ? new char[0] : line.toCharArray();
    }
}
