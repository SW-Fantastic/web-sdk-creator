package org.swdc.websdk.views.requests;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.websdk.core.HttpBodyEntry;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.RequestBodyView;
import org.swdc.websdk.views.cells.HttpEditableCell;
import org.swdc.websdk.views.cells.HttpTypeCell;
import org.swdc.websdk.views.events.ContentChangeEvent;

import java.util.List;

@MultipleImplement(RequestBodyView.class)
public class RequestUrlEncodedBodyView implements RequestBodyView, EventEmitter {

    private TableView<HttpBodyEntry> tableView;

    private TableColumn<HttpBodyEntry,String> colName;

    private TableColumn<HttpBodyEntry, Class> colType;

    private BorderPane root;

    private ObjectMapper mapper = new ObjectMapper();

    private HttpEndpoint endpoint;

    private Events events;

    @Override
    public Node getView() {
        if (tableView == null) {
            root = new BorderPane();

            HBox tool = new HBox();
            tool.setAlignment(Pos.CENTER_LEFT);
            tool.setPadding(new Insets(6,12,6,12));

            Button add = new Button("+");
            add.setMinSize(28,28);
            add.setPrefSize(28,28);
            add.setOnAction(this::addRow);

            Button remove = new Button("-");
            remove.setMinSize(28,28);
            remove.setPrefSize(28,28);
            remove.setOnAction(this::removeRow);

            HBox left = new HBox();
            HBox.setHgrow(left, Priority.ALWAYS);
            left.setAlignment(Pos.CENTER_LEFT);
            left.getChildren().add(new Label("Form Data: "));

            tool.getChildren().addAll(left, remove,add);
            tool.setSpacing(8);
            root.setTop(tool);

            colName = new TableColumn<>("Name");
            colName.setCellFactory(tv -> new HttpEditableCell<>(((item, text) -> {
                item.setName(text);
                update();
            }),HttpBodyEntry::getName));

            colType = new TableColumn<>("Type");
            colType.setCellFactory(tv -> new HttpTypeCell<>(HttpBodyEntry::getType,(item, type) -> {
                item.setType(type);
                update();
            },new Class[]{

                    String.class,
                    Integer.class,
                    Double.class,
                    Boolean.class

            }));
            tableView = new TableView<>();
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
            tableView.getColumns().addAll(colName,colType);
            tableView.setEditable(true);

            root.setCenter(tableView);
        }
        return root;
    }

    private void removeRow(ActionEvent actionEvent) {
        if (endpoint == null) {
            return;
        }
        HttpBodyEntry entry = tableView.getSelectionModel().getSelectedItem();
        if (entry == null) {
            return;
        }
        tableView.getItems().remove(entry);
        update();
    }

    private void addRow(ActionEvent actionEvent) {
        if (this.endpoint == null) {
            return;
        }
        tableView.getItems().add(new HttpBodyEntry());
        update();
    }

    private void update() {
        try {
            String raw = mapper.writeValueAsString(
                    tableView.getItems().toArray()
            );
            endpoint.getRequestBodyRaw().put(getClass(),raw);
            emit(new ContentChangeEvent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "FormUrlEncoded";
    }

    @Override
    public String getRawData() {
        return "";
    }

    @Override
    public void refreshData(HttpEndpoint endpoint) {

        if (tableView == null) {
            getView();
        }

        ObservableList<HttpBodyEntry> entries = tableView.getItems();
        entries.clear();

        String raw = endpoint.getRequestBodyRaw().get(getClass());
        if (raw != null && !raw.isBlank()) {
            JavaType type = mapper.getTypeFactory().constructParametricType(
                    List.class, HttpBodyEntry.class
            );
            try {
                List<HttpBodyEntry> reqEntries = mapper.readValue(raw,type);
                entries.addAll(reqEntries);
            } catch (Exception e) {
            }
        }

        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T extends AbstractEvent> void emit(T t) {
        this.events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
