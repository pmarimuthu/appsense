package com.maybank.appsense.core.filter;

public final class ContainsIgnoreCaseUtil {

    private ContainsIgnoreCaseUtil() {
    }

    /** Case-insensitive substring test that allocates nothing. */
    public static boolean contains(String text, String needle) {
        int last = text.length() - needle.length();
        for (int start = 0; start <= last; start++) {
            if (text.regionMatches(true, start, needle, 0, needle.length())) {
                return true;
            }
        }
        return false;
    }
}
