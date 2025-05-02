package org.swdc.websdk.views.cells;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class HttpEditableTreeCell <T> extends TreeTableCell<T,String> {

    private TextField field;

    private HBox labelGraph;

    private Button disclosure;

    private VBox disclosureWrapper;

    private Label label;

    private TextEditListener<T> cellEditListener = null;

    private Function<T, String> getter;

    private Fontawsome5Service fontawsome5Service;

    private boolean initialized;

    public HttpEditableTreeCell(Fontawsome5Service fontawsome5Service, TextEditListener<T> editListener, Function<T, String> getter) {
        this.fontawsome5Service = fontawsome5Service;
        field = new TextField();
        field.setOnAction(e -> {
            commitEdit(field.getText());
            e.consume();
        });
        field.focusedProperty().addListener(e -> {
            if (!field.isFocused()) {
                commitEdit(field.getText());
            }
        });
        field.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                t.consume();
            }
        });
        this.cellEditListener = editListener;
        this.getter = getter;

        this.label = new Label();

        this.labelGraph = new HBox();
        this.labelGraph.setAlignment(Pos.CENTER_LEFT);
        this.labelGraph.setPadding(new Insets(4));
        this.labelGraph.getChildren().add(label);
        this.labelGraph.getStyleClass().add("field-border");

        this.disclosure = new Button();
        this.disclosure.setId("theIcon");
        this.disclosure.getStyleClass().add("ghost-button");
        this.disclosure.setFont(fontawsome5Service.getRegularFont(FontSize.VERY_SMALL));
        this.disclosure.setOnAction(e -> {

            TreeTableRow<T> currentRow = getTableRow();
            TreeItem<T> currentItem = currentRow.getTreeItem();
            currentItem.setExpanded(!currentItem.isExpanded());

        });

        disclosureWrapper = new VBox();
        disclosureWrapper.setAlignment(Pos.CENTER);
        disclosureWrapper.getChildren().add(disclosure);

    }


    private void expand(Observable observable, Boolean val, Boolean next) {
        TreeTableRow<T> row = getTableRow();
        if (row == null || row.getDisclosureNode() == null) {
            return;
        }
        Button disclosure = (Button) row.getDisclosureNode().lookup("#theIcon");
        String icon = next ? "folder-open" : "folder";
        disclosure.setText(fontawsome5Service.getFontIcon(icon));
    }


    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEditable()) {
            return;
        }
        setGraphic(field);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(labelGraph);
    }

    @Override
    public void commitEdit(String newValue) {
        setGraphic(labelGraph);
        cellEditListener.changed(getTableRow().getItem(), newValue);
        label.setText(newValue);
        super.commitEdit(newValue);
    }

    @Override
    protected void updateItem(String item, boolean empty) {

        super.updateItem(item, empty);

        if (empty) {

            disclosureWrapper.setVisible(false);
            setGraphic(null);

        } else {

            TreeTableRow<T> row =  getTableRow();
            TreeItem<T> rowItem = row.getTreeItem();
            if (!initialized) {
                final ChangeListener<Boolean> changeListener = this::expand;
                row.treeItemProperty().addListener((obs, old, next) -> {

                    if (old != null) {
                        old.expandedProperty().removeListener(changeListener);
                    }

                    if (next != null) {
                        next.expandedProperty().addListener(changeListener);
                    }

                });
                initialized = true;
            }

            row.setDisclosureNode(disclosureWrapper);
            if (rowItem.isLeaf()) {
                disclosureWrapper.setVisible(false);
            } else {
                disclosureWrapper.setVisible(true);
            }

            String icon = rowItem.isExpanded() ? "folder-open" : "folder";
            disclosure.setText(fontawsome5Service.getFontIcon(icon));

            field.setText(getter.apply(getTableRow().getItem()));
            label.setText(getter.apply(getTableRow().getItem()));
            if (isEditing()) {
                setGraphic(field);
            } else {
                setGraphic(labelGraph);
            }

        }

    }


}
