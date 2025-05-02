package org.swdc.websdk.views.events;

import org.swdc.dependency.event.AbstractEvent;
import org.swdc.websdk.core.HttpEndpoints;

public class ListUnselectEvent extends AbstractEvent {

    public ListUnselectEvent(HttpEndpoints message) {
        super(message);
    }

}
