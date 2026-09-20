package com.maybank.appsense.core.event;

import com.maybank.appsense.core.model.DeploymentMarkerRecord;

public record DeploymentDetectedEvent(DeploymentMarkerRecord marker) implements AppEvent {
}
