package org.swdc.websdk.views.cells;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpMethod;

public class HttpMethodTreeCell extends TreeTableCell<HttpEndpointItem, HttpMethod> {

    private HBox root;

    private HBox container;

    private Label label;

    public HttpMethodTreeCell() {
        label = new Label();
        root = new HBox();

        container = new HBox();
        container.setFillHeight(false);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(6));
        HBox.setHgrow(container, Priority.NEVER);

        container.getChildren().add(label);

        root.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(container);
    }

    @Override
    protected void updateItem(HttpMethod item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {

            setGraphic(null);

        } else {

            HttpEndpointItem cell = getTableRow().getItem();
            HttpEndpoint endpoint = cell.getAsEndpoint();
            if (endpoint == null) {
                setGraphic(null);
                return;
            }

            container.getStyleClass().clear();
            container.getStyleClass().add("http-" + endpoint.getMethod().name().toLowerCase());
            label.setText(endpoint.getMethod().name());

            setGraphic(root);
        }
    }
}
