package com.maybank.appsense.core.event;

import com.maybank.appsense.core.model.GapMarkerRecord;

public record GapMarkerEvent(GapMarkerRecord marker) implements AppEvent {
}
