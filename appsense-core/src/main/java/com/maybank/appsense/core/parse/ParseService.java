package com.maybank.appsense.core.parse;

import com.maybank.appsense.core.model.LogLineRecord;
import com.maybank.appsense.core.model.ServerType;
import java.util.List;
import java.util.Optional;

public class ParseService {

    private final List<LineParser> parsers;

    public ParseService(ServerType serverType) {
        this.parsers = switch (serverType) {
            case JBOSS -> List.of(new JBossLineParser(), new GenericLineParser());
            case WEBLOGIC -> List.of(new WebLogicLineParser(), new GenericLineParser());
        };
    }

    /** Parses a line that starts a new log entry. Continuation lines yield an empty result. */
    public Optional<LogLineRecord> parseHeader(String line) {
        for (LineParser parser : parsers) {
            Optional<LogLineRecord> parsed = parser.parse(line);
            if (parsed.isPresent()) {
                return parsed;
            }
        }
        return Optional.empty();
    }
}
