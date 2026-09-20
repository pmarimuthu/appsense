package com.maybank.appsense.cli.color;

/** ANSI 16-colour codes optimised for cmd's default black background. */
public final class AnsiColorUtil {

    public static final String ERROR  = "\u001B[91m";
    public static final String WARN   = "\u001B[93m";
    public static final String INFO   = "\u001B[37m";
    public static final String DIM    = "\u001B[90m";
    public static final String DEPLOY = "\u001B[96m";
    public static final String BOLD   = "\u001B[1m";
    public static final String RESET  = "\u001B[0m";

    private AnsiColorUtil() {}
}
