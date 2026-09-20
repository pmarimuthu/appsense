package com.maybank.appsense.core.event;

import java.time.Instant;

/** Deployment time is null when no deployment was detected during the pause. */
public record PauseStateEvent(
        boolean paused,
        String elapsedText,
        long newLines,
        boolean overflowed,
        Instant deploymentDetectedAt
) implements AppEvent {

    public boolean hasDeployment() {
        return deploymentDetectedAt != null;
    }
}
