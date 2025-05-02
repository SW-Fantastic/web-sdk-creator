package org.swdc.websdk.views.cells;

import javafx.scene.control.ListCell;

public class TypeListCell extends ListCell<Class> {

    @Override
    protected void updateItem(Class item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setText(item.getSimpleName());
        }
    }
}
