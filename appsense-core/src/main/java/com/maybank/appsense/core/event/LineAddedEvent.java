package com.maybank.appsense.core.event;

import com.maybank.appsense.core.model.StreamItem;
import java.util.List;

/**
 * Carries a batch of stream items. When {@code reset} is true the face must clear its view and show
 * exactly these items; otherwise the items are appended.
 */
public record LineAddedEvent(List<StreamItem> items, boolean reset) implements AppEvent {
}
