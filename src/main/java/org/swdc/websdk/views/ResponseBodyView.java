package org.swdc.websdk.views;

import javafx.scene.Node;
import org.swdc.dependency.annotations.ImplementBy;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.requests.ResponseBodyBlankView;
import org.swdc.websdk.views.requests.ResponseBodyJsonView;

@ImplementBy({
        ResponseBodyJsonView.class,
        ResponseBodyBlankView.class
})
public interface ResponseBodyView {

    Node getView();

    String getName();

    void refreshData(HttpEndpoint endpoint);

}
