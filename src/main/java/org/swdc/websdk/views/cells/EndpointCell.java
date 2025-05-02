package org.swdc.websdk.views.cells;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;

public class EndpointCell extends ListCell<HttpEndpoint> {

    protected HBox box;

    private HBox methodWrapper;

    private Label method;

    protected Label label;

    private boolean initialized;

    public EndpointCell() {
        box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(2));
        box.setSpacing(8);

        label = new Label();
        label.setText("");

        methodWrapper = new HBox();
        methodWrapper.setPadding(new Insets(2,6,2,6));
        methodWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(methodWrapper, Priority.NEVER);

        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(wrapper,Priority.ALWAYS);
        wrapper.getChildren().add(label);

        method = new Label();
        methodWrapper.getChildren().add(method);
        box.getChildren().addAll(wrapper,methodWrapper);

        itemProperty().addListener(this::onItemChanged);


    }

    private void onItemChanged(ObservableValue<? extends HttpEndpoint> observableValue, HttpEndpoint old, HttpEndpoint next) {
        if (old != null) {
            old.methodProperty().removeListener(this::onMethodChanged);
        }
        if (next != null) {
            next.methodProperty().addListener(this::onMethodChanged);
            label.textProperty().unbind();
            label.textProperty().bind(next.urlProperty());
        }
    }


    @Override
    protected void updateItem(HttpEndpoint item, boolean empty) {
        super.updateItem(item,empty);
        if (empty) {
            setGraphic(null);
        } else {
            if (!initialized) {
                label.maxWidthProperty().unbind();
                label.maxWidthProperty().bind(getListView().widthProperty().divide(2));
                label.getWidth();
                initialized = true;
            }
            onMethodChanged(null);
            setGraphic(box);
        }
    }


    private void onMethodChanged(Observable observable) {
        HttpEndpoint item = getItem();
        if (item == null) {
            return;
        }
        if (item.getMethod().name().equals(method.getText())) {
            return;
        }
        methodWrapper.getStyleClass().clear();
        methodWrapper.getStyleClass().add("http-" + item.getMethod().name().toLowerCase());
        method.setText(item.getMethod().name());
    }

}
