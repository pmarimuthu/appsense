package com.maybank.appsense.core.model;

public record AppInfoRecord(
        String name,
        String tagline,
        String version,
        String buildNumber,
        String buildDate,
        String javaVersion,
        String osName
) {
}
