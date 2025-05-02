package org.swdc.websdk.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.swdc.fx.FXResources;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.ViewController;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.HttpMethod;
import org.swdc.websdk.core.generator.OpenAPIParser;
import org.swdc.websdk.views.cells.HttpEditableTreeCell;
import org.swdc.websdk.views.cells.HttpEndpointItem;
import org.swdc.websdk.views.cells.HttpMethodTreeCell;
import org.swdc.websdk.views.cells.LabeledTreeCell;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;


@Singleton
public class OpenAPIImportController extends ViewController<OpenAPIImportView> {

    private static final DataFormat objectFormat = new DataFormat("application/object");

    private SimpleBooleanProperty menuDisabled = new SimpleBooleanProperty(true);

    private OpenAPIParser parser = new OpenAPIParser();

    @Inject
    private Logger logger;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private FXResources resources;

    @FXML
    private TextField txtUrl;

    @FXML
    private TreeTableView<HttpEndpointItem> treeTableView;

    @FXML
    private TreeTableColumn<HttpEndpointItem,String> colName;

    @FXML
    private TreeTableColumn<HttpEndpointItem, HttpMethod> colMethod;

    @FXML
    private TreeTableColumn<HttpEndpointItem,String> colUrl;

    private List<HttpEndpoints> loaded = null;

    private boolean doImport = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initTableContextMenu();

        treeTableView.setRoot(new TreeItem<>());
        treeTableView.setShowRoot(false);
        treeTableView.getSelectionModel().selectedItemProperty().addListener((obs,old,next) -> {
            menuDisabled.set(next == null);
        });
        treeTableView.setRowFactory(this::rowFactory);

        colName.setCellFactory(v -> new HttpEditableTreeCell<>(
                fontawsome5Service,
                (it, val) -> {
                    if (it.getAsEndpointSet() != null) {
                        it.getAsEndpointSet().setName(val);
                    } else if (it.getAsEndpoint() != null) {
                        it.getAsEndpoint().setName(val);
                    }
                }, (it) -> {
                    if (it.getAsEndpoint() != null) {
                        return it.getAsEndpoint().getName();
                    } else if (it.getAsEndpointSet() != null) {
                        return it.getAsEndpointSet().getName();
                    }
                    return "";
                }));

        colMethod.setCellFactory(v -> new HttpMethodTreeCell());
        colUrl.setCellFactory(v -> new LabeledTreeCell<>(it -> {
            if (it == null || it.getAsEndpoint() == null) {
                return "";
            }
            HttpEndpoint endpoint = it.getAsEndpoint();
            return endpoint.getUrl();
        }));

    }

    private TreeTableRow<HttpEndpointItem> rowFactory(TreeTableView<HttpEndpointItem> httpEndpointItemTreeTableView) {
        TreeTableRow<HttpEndpointItem> row = new TreeTableRow<>();

        row.setOnDragDetected(d -> {

            if (row.isEmpty() || row.getTreeItem() == null) {
                return;
            }

            TreeItem<HttpEndpointItem> it = row.getTreeItem();
            HttpEndpointItem item = it.getValue();
            if (item.getAsEndpoint() == null) {
                return;
            }

            ClipboardContent content = new ClipboardContent();
            content.put(objectFormat,row.getIndex());

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.WHITE);
            WritableImage snapshot = row.snapshot(parameters,null);

            Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
            dragboard.setContent(content);
            dragboard.setDragView(snapshot);

        });

        row.setOnDragOver( d -> {

            if (row.isEmpty() || row.getTreeItem() == null) {
                return;
            }

            TreeItem<HttpEndpointItem> it = row.getTreeItem();
            HttpEndpointItem item = it.getValue();
            if (item.getAsEndpointSet() == null) {
                return;
            }

            Dragboard dragboard = d.getDragboard();
            if (dragboard.getContent(objectFormat) != null) {
                d.acceptTransferModes(TransferMode.MOVE);
                d.consume();
            }

        });

        row.setOnDragDropped( d -> {

            if (row.isEmpty() || row.getTreeItem() == null) {
                return;
            }

            TreeItem<HttpEndpointItem> targetNode = row.getTreeItem();
            HttpEndpointItem targetItem = targetNode.getValue();
            if (targetItem.getAsEndpointSet() == null) {
                return;
            }

            Dragboard dragboard = d.getDragboard();
            if (dragboard.getContent(objectFormat) != null) {

                Integer index = (Integer) dragboard.getContent(objectFormat);
                TreeItem<HttpEndpointItem> source = treeTableView.getTreeItem(index);
                TreeItem<HttpEndpointItem> sourceParent = source.getParent();

                HttpEndpoint sourceValue = source.getValue().getAsEndpoint();
                HttpEndpoints sourceSet = sourceParent.getValue().getAsEndpointSet();
                HttpEndpoints targetSet = targetItem.getAsEndpointSet();

                sourceSet.getEndpoints().remove(sourceValue);
                sourceParent.getChildren().remove(source);

                targetSet.getEndpoints().add(sourceValue);
                targetNode.getChildren().add(source);
                d.setDropCompleted(true);

            }

        });

        return row;
    }


    private void initTableContextMenu() {

        ResourceBundle bundle = resources.getResourceBundle();

        MenuItem tip = new MenuItem(bundle.getString(LanguageKeys.DLG_DOUBLE_CLICK_EDIT));
        tip.setDisable(true);

        MenuItem actionTrash = new MenuItem(bundle.getString(LanguageKeys.DLG_DELETE_ITEM));
        actionTrash.setOnAction(e -> {

            TreeItem<HttpEndpointItem> it = treeTableView.getSelectionModel().getSelectedItem();
            if (it == null) {
                return;
            }

            TreeItem<HttpEndpointItem> parent = it.getParent();

            HttpEndpointItem item = it.getValue();
            if (item.getAsEndpointSet() != null) {
                loaded.remove(item.getAsEndpointSet());
            } else if (item.getAsEndpoint() != null) {
                HttpEndpointItem parentValue = parent.getValue();
                HttpEndpoints endpoints = parentValue.getAsEndpointSet();
                endpoints.getEndpoints().remove(item.getAsEndpoint());
            }

            parent.getChildren().remove(it);

        });

        actionTrash.disableProperty().bind(menuDisabled);

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(tip,new SeparatorMenuItem(),actionTrash);
        treeTableView.setContextMenu(menu);
    }

    @FXML
    public void browserSource() {

        ResourceBundle bundle = resources.getResourceBundle();

        try {
            if(getView().isUrl()) {

                if (txtUrl.getText().isBlank()) {
                    return;
                }

                TreeItem<HttpEndpointItem> root = treeTableView.getRoot();
                List<HttpEndpoints> endpoints = parser.parseDefinitionUrl(txtUrl.getText());
                buildTree(root,endpoints);
                this.loaded = endpoints;

            } else {

                FileChooser chooser = new FileChooser();
                chooser.setTitle(bundle.getString(LanguageKeys.OPEN));
                chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("open api definition", ".json", "*.yaml", "*.yml"));
                File file = chooser.showOpenDialog(getView().getStage());
                if (file == null) {
                    return;
                }

                FileInputStream ins = new FileInputStream(file);
                TreeItem<HttpEndpointItem> root = treeTableView.getRoot();
                String content = new String(ins.readAllBytes());
                List<HttpEndpoints> endpoints = parser.parseDefinition(content);
                buildTree(root,endpoints);
                this.loaded = endpoints;
                ins.close();

                txtUrl.setText(file.getAbsolutePath());

            }
        } catch (Exception e) {
            logger.error("Failed to read resource " , e);
            Alert alert = getView().alert(
                    bundle.getString(LanguageKeys.DLG_WARN),
                    bundle.getString(LanguageKeys.DLG_UNKNOWN_ERR),
                    Alert.AlertType.ERROR
            );
            alert.showAndWait();

        }

    }

    @FXML
    public void submit() {
        this.doImport = true;
        getView().hide();
    }

    @FXML
    public void close() {
        this.doImport = false;
        this.loaded = Collections.emptyList();
        getView().hide();
    }

    private void buildTree(TreeItem<HttpEndpointItem> parent, List endpoints) {

        parent.getChildren().clear();
        for (Object children : endpoints) {
            TreeItem<HttpEndpointItem> item = new TreeItem<>(new HttpEndpointItem(children));
            parent.getChildren().add(item);
            if (children instanceof HttpEndpoints) {

                HttpEndpoints endpointSet = (HttpEndpoints) children;
                buildTree(item, endpointSet.getEndpoints());

            }
        }

    }

    public List<HttpEndpoints> getAndReset() {

        if (this.loaded == null) {
            return Collections.emptyList();
        }
        List<HttpEndpoints> endpoints = new ArrayList<>(this.loaded);
        reset();

        return endpoints;
    }

    public void reset() {

        txtUrl.setText("");
        treeTableView.getRoot().getChildren().clear();
        loaded = new ArrayList<>();
        doImport = false;

    }

    public boolean isDoImport() {
        return doImport;
    }
}
