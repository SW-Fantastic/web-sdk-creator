package org.swdc.websdk.views.cells;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.websdk.core.HttpMethod;

public class MethodCell extends ListCell<HttpMethod> {

    private HBox root;

    private HBox container;

    private Label label;

    public MethodCell() {

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

            container.getStyleClass().clear();
            container.getStyleClass().add("http-" + item.name().toLowerCase());
            label.setText(item.name());

            setGraphic(root);
        }
    }
}
