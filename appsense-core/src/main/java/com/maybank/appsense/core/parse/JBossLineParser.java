package com.maybank.appsense.core.parse;

import com.maybank.appsense.core.config.ParseConstant;
import com.maybank.appsense.core.model.LogLevel;
import com.maybank.appsense.core.model.LogLineRecord;
import java.time.DateTimeException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JBossLineParser implements LineParser {

    private static final Logger log = LoggerFactory.getLogger(JBossLineParser.class);

    @Override
    public Optional<LogLineRecord> parse(String line) {
        Matcher matcher = ParseConstant.JBOSS_LINE.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new LogLineRecord(
                    TimestampUtil.fromIsoParts(matcher),
                    LogLevel.parse(matcher.group(ParseConstant.GROUP_LEVEL)),
                    matcher.group(ParseConstant.GROUP_LOGGER),
                    matcher.group(ParseConstant.GROUP_MESSAGE),
                    List.of()));
        } catch (DateTimeException ex) {
            log.debug("Line looked like a JBoss header but its timestamp was invalid", ex);
            return Optional.empty();
        }
    }
}
