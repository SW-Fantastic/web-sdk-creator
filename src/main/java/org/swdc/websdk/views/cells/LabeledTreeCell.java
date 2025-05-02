package org.swdc.websdk.views.cells;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;

import java.util.function.Function;

public class LabeledTreeCell<T> extends TreeTableCell<T,String> {

    private HBox labelGraph;

    private Label label;

    private Function<T, String> getter;

    public LabeledTreeCell(Function<T,String> getter) {

        this.getter = getter;
        this.label = new Label();

        this.labelGraph = new HBox();
        this.labelGraph.setAlignment(Pos.CENTER_LEFT);
        this.labelGraph.setPadding(new Insets(4));
        this.labelGraph.getChildren().add(label);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            return;
        }

        String txt = getter.apply(getTableRow().getItem());
        if (txt != null) {
            label.setText(txt);
            setGraphic(labelGraph);
        } else {
            setGraphic(null);
        }

    }
}
