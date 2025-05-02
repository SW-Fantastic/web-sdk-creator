package org.swdc.websdk.views.events;

import org.swdc.dependency.event.AbstractEvent;
import org.swdc.websdk.core.HttpEndpoints;

public class EndpointSetDeleteEvent extends AbstractEvent {

    public EndpointSetDeleteEvent(HttpEndpoints message) {
        super(message);
    }

}
