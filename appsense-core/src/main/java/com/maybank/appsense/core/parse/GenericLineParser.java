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

class GenericLineParser implements LineParser {

    private static final Logger log = LoggerFactory.getLogger(GenericLineParser.class);

    @Override
    public Optional<LogLineRecord> parse(String line) {
        Matcher matcher = ParseConstant.GENERIC_LINE.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new LogLineRecord(
                    TimestampUtil.fromIsoParts(matcher),
                    LogLevel.parse(matcher.group(ParseConstant.GROUP_LEVEL)),
                    pickLogger(matcher),
                    matcher.group(ParseConstant.GROUP_MESSAGE),
                    List.of()));
        } catch (DateTimeException ex) {
            log.debug("Line looked like a log header but its timestamp was invalid", ex);
            return Optional.empty();
        }
    }

    private String pickLogger(Matcher matcher) {
        String bracketed = matcher.group(ParseConstant.GROUP_BRACKET_LOGGER);
        if (bracketed != null) {
            return bracketed;
        }
        String plain = matcher.group(ParseConstant.GROUP_LOGGER);
        return plain == null ? "" : plain;
    }
}
