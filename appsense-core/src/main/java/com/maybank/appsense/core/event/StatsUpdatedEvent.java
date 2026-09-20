package com.maybank.appsense.core.event;

import com.maybank.appsense.core.model.StatsSnapshotRecord;
import com.maybank.appsense.core.model.SummaryRowRecord;
import java.util.List;

public record StatsUpdatedEvent(StatsSnapshotRecord stats, List<SummaryRowRecord> summary) implements AppEvent {
}
