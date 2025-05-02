package org.swdc.websdk.views.cells;

import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;

public class HttpEndpointItem {

    private Object item;

    public HttpEndpointItem(Object o) {
        this.item = o;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public HttpEndpoint getAsEndpoint() {
        if (item instanceof HttpEndpoint) {
            return (HttpEndpoint) item;
        }
        return null;
    }

    public HttpEndpoints getAsEndpointSet() {
        if (item instanceof HttpEndpoints) {
            return (HttpEndpoints) item;
        }
        return null;
    }

}
