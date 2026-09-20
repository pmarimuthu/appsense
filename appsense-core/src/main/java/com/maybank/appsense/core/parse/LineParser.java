package com.maybank.appsense.core.parse;

import com.maybank.appsense.core.model.LogLineRecord;
import java.util.Optional;

interface LineParser {

    Optional<LogLineRecord> parse(String line);
}
