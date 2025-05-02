package org.swdc.websdk.views.events;

import org.swdc.dependency.event.AbstractEvent;
import org.swdc.websdk.core.HttpEndpoint;

public class EndpointChangedEvent extends AbstractEvent {

    public EndpointChangedEvent(HttpEndpoint message) {
        super(message);
    }

}
