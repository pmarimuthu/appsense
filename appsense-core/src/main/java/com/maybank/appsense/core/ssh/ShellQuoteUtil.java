package com.maybank.appsense.core.ssh;

import com.maybank.appsense.core.config.SshConstant;

public final class ShellQuoteUtil {

    private ShellQuoteUtil() {
    }

    public static String quote(String value) {
        return SshConstant.SHELL_QUOTE
                + value.replace(SshConstant.SHELL_QUOTE, SshConstant.SHELL_QUOTE_ESCAPED)
                + SshConstant.SHELL_QUOTE;
    }
}
