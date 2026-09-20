package com.maybank.appsense.core.deploy;

import com.maybank.appsense.core.config.AppConstant;
import com.maybank.appsense.core.config.DeployConstant;
import com.maybank.appsense.core.config.ResourcePropertiesUtil;
import com.maybank.appsense.core.exception.ConfigException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MarkerPatternUtil {

    private static final Logger log = LoggerFactory.getLogger(MarkerPatternUtil.class);

    private MarkerPatternUtil() {
    }

    /** Built-in JBoss and WebLogic codes followed by the profile's own patterns. */
    public static List<Pattern> compile(List<String> customMarkers) throws ConfigException {
        Properties defaults = ResourcePropertiesUtil.load(AppConstant.DEFAULT_MARKERS_RESOURCE);
        List<Pattern> patterns = new ArrayList<>();
        for (String key : List.of(DeployConstant.JBOSS_CODES_KEY, DeployConstant.WEBLOGIC_CODES_KEY)) {
            for (String code : split(defaults.getProperty(key))) {
                patterns.add(Pattern.compile(Pattern.quote(code)));
            }
        }
        customMarkers.forEach(custom -> patterns.add(compileCustom(custom)));
        return patterns;
    }

    public static Pattern artifactPattern() throws ConfigException {
        Properties defaults = ResourcePropertiesUtil.load(AppConstant.DEFAULT_MARKERS_RESOURCE);
        return Pattern.compile(defaults.getProperty(DeployConstant.ARTIFACT_REGEX_KEY));
    }

    private static List<String> split(String codes) {
        return Arrays.stream(codes.split(DeployConstant.CODES_SEPARATOR)).map(String::trim).toList();
    }

    private static Pattern compileCustom(String custom) {
        try {
            return Pattern.compile(custom);
        } catch (PatternSyntaxException ex) {
            log.warn("Custom deployment marker {} is not a valid regex; matching it as plain text", custom, ex);
            return Pattern.compile(Pattern.quote(custom));
        }
    }
}
