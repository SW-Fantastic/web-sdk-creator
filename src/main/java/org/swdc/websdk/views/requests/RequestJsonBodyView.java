package org.swdc.websdk.views.requests;

import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.fx.FXResources;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.LanguageKeys;
import org.swdc.websdk.views.RequestBodyView;
import org.swdc.websdk.views.events.ContentChangeEvent;

import java.time.Duration;
import java.util.ResourceBundle;

@MultipleImplement(RequestBodyView.class)
public class RequestJsonBodyView implements RequestBodyView, EventEmitter {


    private HttpEndpoint endpoint;

    private VirtualizedScrollPane<CodeArea> scrollPane;

    private CodeArea codeArea;

    private HighlightHelper highlightHelper;

    private Events events;

    @Inject
    private FXResources resources;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    private boolean resetLock;

    @Override
    public Node getView() {
        if (codeArea == null) {
            codeArea = new CodeArea();
            highlightHelper = new HighlightHelper();
            codeArea.plainTextChanges().successionEnds(Duration.ofMillis(200))
                    .subscribe(i -> {
                        try {
                            codeArea.setStyleSpans(0,highlightHelper.highlight(codeArea.getText()));
                        } catch (Exception ignore) {
                        }
                    });
            codeArea.textProperty().addListener(o -> {
                if (endpoint != null && codeArea.isUndoAvailable() && !resetLock) {

                    endpoint.getRequestBodyRaw().put(getClass(),codeArea.getText());
                    emit(new ContentChangeEvent());

                }
            });
            scrollPane = new VirtualizedScrollPane<>(codeArea);
            codeArea.getStyleClass().add("code-area");
            initContextMenu();
        }
        return scrollPane;
    }

    private void initContextMenu() {

        ResourceBundle bundle = resources.getResourceBundle();
        MenuItem copy = new MenuItem();
        copy.setText(bundle.getString(LanguageKeys.COPY));
        copy.setGraphic(createIcon("clone"));
        copy.setOnAction(e -> {
            codeArea.copy();
        });

        MenuItem paste = new MenuItem();
        paste.setText(bundle.getString(LanguageKeys.PASTE));
        paste.setGraphic(createIcon("clipboard"));
        paste.setOnAction(e -> {
            codeArea.paste();
        });

        MenuItem undo = new MenuItem();
        undo.setText(bundle.getString(LanguageKeys.UNDO));
        codeArea.undoAvailableProperty().subscribe(v -> {
            undo.setDisable(!v);
        });
        undo.setOnAction(e -> {
            codeArea.undo();
        });

        MenuItem redo = new MenuItem(bundle.getString(LanguageKeys.REDO));
        codeArea.redoAvailableProperty().subscribe(v -> {
            redo.setDisable(!v);
        });
        redo.setOnAction(e -> {
            codeArea.redo();
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(copy,paste,undo,redo);
        codeArea.setContextMenu(contextMenu);
    }

    private Label createIcon(String icon) {
        Label label = new Label();
        label.setFont(fontawsome5Service.getRegularFont(FontSize.SMALLEST));
        label.setText(fontawsome5Service.getFontIcon(icon));
        label.setPadding(new Insets(0));
        return label;
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getRawData() {
        return codeArea.getText();
    }

    @Override
    public void refreshData(HttpEndpoint endpoint) {
        if (codeArea == null) {
            getView();
        }

        resetLock = true;
        this.endpoint = endpoint;
        this.codeArea.clear();

        String raw = this.endpoint.getRequestBodyRaw().get(getClass());
        if (raw != null) {
            codeArea.appendText(raw);
        }
        codeArea.getUndoManager().forgetHistory();
        resetLock = false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public <T extends AbstractEvent> void emit(T t) {
        events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
