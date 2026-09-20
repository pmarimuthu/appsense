package com.maybank.appsense.core.session;

import com.maybank.appsense.core.model.StreamItem;

record StreamInput(String rawLine, StreamItem item) {

    static StreamInput ofLine(String rawLine) {
        return new StreamInput(rawLine, null);
    }

    static StreamInput ofItem(StreamItem item) {
        return new StreamInput(null, item);
    }

    boolean isLine() {
        return rawLine != null;
    }
}
