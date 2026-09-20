package com.maybank.appsense.core.ssh;

import java.util.Arrays;
import java.util.List;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.session.ClientSession;

final class PasswordUserInteraction implements UserInteraction {

    private final char[] password;

    PasswordUserInteraction(char[] password) {
        this.password = password;
    }

    @Override
    public boolean isInteractionAllowed(ClientSession session) {
        return true;
    }

    @Override
    public void serverVersionInfo(ClientSession session, List<String> lines) {
    }

    @Override
    public void welcome(ClientSession session, String banner, String lang) {
    }

    @Override
    public String[] interactive(ClientSession session, String name, String instruction, String lang,
                                String[] prompt, boolean[] echo) {
        String[] answers = new String[prompt.length];
        Arrays.fill(answers, new String(password));
        return answers;
    }

    @Override
    public String getUpdatedPassword(ClientSession session, String prompt, String lang) {
        return null;
    }
}
