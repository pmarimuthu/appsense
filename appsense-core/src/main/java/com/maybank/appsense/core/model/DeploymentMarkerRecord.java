package com.maybank.appsense.core.model;

import java.time.Instant;

public record DeploymentMarkerRecord(Instant timestamp, String warName) implements StreamItem {
}
