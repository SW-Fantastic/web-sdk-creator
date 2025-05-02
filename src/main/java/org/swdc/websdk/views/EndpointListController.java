package org.swdc.websdk.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.dependency.annotations.Prototype;
import org.swdc.fx.view.ViewController;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.views.cells.EndpointCell;
import org.swdc.websdk.views.events.ContentChangeEvent;
import org.swdc.websdk.views.events.EndpointChangedEvent;
import org.swdc.websdk.views.events.EndpointSetDeleteEvent;
import org.swdc.websdk.views.events.ListUnselectEvent;

import java.net.URL;
import java.util.ResourceBundle;

@Prototype
public class EndpointListController extends ViewController<EndpointListView> {

    private HttpEndpoints endpoints;

    private ContextMenu contextMenu;

    @FXML
    private ListView<HttpEndpoint> listView;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

        listView.setCellFactory(lv -> new EndpointCell());
        listView.setOnMouseClicked(this::changed);

        MenuItem itemRename = new MenuItem(resourceBundle.getString(LanguageKeys.RENAME));
        itemRename.setOnAction(this::onRename);

        MenuItem itemTrash = new MenuItem(resourceBundle.getString(LanguageKeys.TRASH));
        itemTrash.setOnAction(this::onRemoveSet);

        contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);
        contextMenu.getItems().addAll(itemRename,itemTrash);
    }

    @EventListener(type = ListUnselectEvent.class)
    public void unselect(ListUnselectEvent event) {
        HttpEndpoints eps = event.getMessage();
        if (eps.equals(endpoints)) {
            return;
        }
        listView.getSelectionModel().clearSelection();
    }

    private void changed(MouseEvent observable) {

        HttpEndpoint endpoint = listView.getSelectionModel().getSelectedItem();
        if (endpoint == null) {
            return;
        }
        getView().emit(new EndpointChangedEvent(endpoint));

    }

    void setEndPoints(HttpEndpoints endpoints) {
        this.endpoints = endpoints;
        refreshView();
    }

    @FXML
    public void onAdd() {

        AddEndpointModal modal = getView().getView(AddEndpointModal.class);
        HttpEndpoint endpoint = modal.showCreateDialog();
        if (endpoint == null) {
            return;
        }

        if (endpoints.getEndpoints().contains(endpoint)) {
            Alert alert = getView().alert("提示","该端点已经存在。", Alert.AlertType.ERROR);
            alert.showAndWait();
            return;
        }

        endpoints.getEndpoints().add(endpoint);
        refreshView();

        listView.getSelectionModel().select(endpoint);
        getView().emit(new EndpointChangedEvent(endpoint));
        getView().emit(new ContentChangeEvent());

    }


    @FXML
    public void onRemove() {

        HttpEndpoint endpoint = listView.getSelectionModel().getSelectedItem();
        if (endpoint == null ) {
            return;
        }
        listView.getItems().remove(endpoint);
        getView().emit(new ContentChangeEvent());

    }

    @FXML
    public void onShowMenu(ActionEvent event) {

        Button actionButton = getView().findById("actions");
        contextMenu.show(actionButton, Side.BOTTOM,0,4);

    }


    public void onRemoveSet(ActionEvent event) {

        getView().emit(new EndpointSetDeleteEvent(endpoints));

    }


    private void onRename(ActionEvent event) {

        EditEndpointSetModal endpointSetModal = getView().getView(EditEndpointSetModal.class);
        String name = endpointSetModal.showEditModal(endpoints.getName());
        if (name == null || name.isBlank()) {
            return;
        }
        endpoints.setName(name);

    }

    private void refreshView() {

        listView.getItems().clear();
        listView.getItems().addAll(endpoints.getEndpoints());

    }

}
