package com.maybank.appsense.core.config;

public final class DeployConstant {

    public static final String JBOSS_CODES_KEY = "jboss.codes";
    public static final String WEBLOGIC_CODES_KEY = "weblogic.codes";
    public static final String ARTIFACT_REGEX_KEY = "artifact.regex";
    public static final String CODES_SEPARATOR = ",";
    public static final long DEDUP_SECONDS = 120L;
    public static final int ARTIFACT_GROUP_QUOTED = 1;
    public static final int ARTIFACT_GROUP_PLAIN = 2;

    private DeployConstant() {
    }
}
