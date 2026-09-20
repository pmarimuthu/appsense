package com.maybank.appsense.core.event;

public sealed interface AppEvent
        permits LineAddedEvent, StatsUpdatedEvent, DeploymentDetectedEvent,
                ConnectionStateEvent, PauseStateEvent, GapMarkerEvent, NoticeEvent {
}
