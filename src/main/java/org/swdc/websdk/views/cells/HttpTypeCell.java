package org.swdc.websdk.views.cells;

import javafx.beans.Observable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

import java.util.function.Function;

public class HttpTypeCell<T> extends TableCell<T, Class> {

    private Function<T,Class> getter;

    private TypeEditListener<T> listener;

    private ComboBox<Class> typeCombobox;

    public HttpTypeCell(Function<T, Class> getter, TypeEditListener<T> listener, Class[] types) {
        this.getter = getter;
        this.listener = listener;
        this.typeCombobox = new ComboBox<>();
        this.typeCombobox.getItems().addAll(types);
        this.typeCombobox.setButtonCell(new TypeListCell());
        this.typeCombobox.setCellFactory(lv -> new TypeListCell());
        this.typeCombobox.getSelectionModel().selectedItemProperty().addListener(this::changed);

    }

    private void changed(Observable observable) {

        TableRow<T> row = getTableRow();
        if (row == null || row.getItem() == null) {
            return;
        }

        this.listener.changed(row.getItem(),typeCombobox.getSelectionModel().getSelectedItem());

    }

    @Override
    protected void updateItem(Class item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            typeCombobox.getSelectionModel().select(getter.apply(getTableRow().getItem()));
            setGraphic(typeCombobox);
        }
    }
}
