package com.maybank.appsense.core.model;

import java.time.Instant;

public record GapMarkerRecord(Instant timestamp) implements StreamItem {
}
