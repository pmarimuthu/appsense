package com.maybank.appsense.core.event;

public record ConnectionStateEvent(State state, String profileName) implements AppEvent {

    public enum State {
        CONNECTING, CONNECTED, RECONNECTING, DISCONNECTED
    }
}
