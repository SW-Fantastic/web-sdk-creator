package org.swdc.websdk.views;

import javafx.scene.Node;
import org.swdc.dependency.annotations.ImplementBy;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.requests.RequestBlankView;
import org.swdc.websdk.views.requests.RequestJsonBodyView;
import org.swdc.websdk.views.requests.RequestUrlEncodedBodyView;


@ImplementBy({
        RequestJsonBodyView.class,
        RequestUrlEncodedBodyView.class,
        RequestBlankView.class
})
public interface RequestBodyView {

    Node getView();

    String getName();

    String getRawData();

    void refreshData(HttpEndpoint endpoint);

}
