package org.swdc.websdk.views.requests;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.ResponseBodyView;

@MultipleImplement(ResponseBodyView.class)
public class ResponseBodyBlankView implements ResponseBodyView {

    private VBox root = null;

    @Override
    public Node getView() {
        if (root == null) {
            root = new VBox();
            root.setPadding(new Insets(12));
            Label label = new Label();
            label.setText("This request does not have a response body");
            root.getChildren().add(label);
        }
        return root;
    }

    @Override
    public String getName() {
        return "Empty";
    }

    @Override
    public void refreshData(HttpEndpoint endpoint) {

    }

    @Override
    public String toString() {
        return getName();
    }
}
