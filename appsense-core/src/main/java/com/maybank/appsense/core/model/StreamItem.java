package com.maybank.appsense.core.model;

import java.time.Instant;

public sealed interface StreamItem permits LogLineRecord, DeploymentMarkerRecord, GapMarkerRecord {

    Instant timestamp();
}
