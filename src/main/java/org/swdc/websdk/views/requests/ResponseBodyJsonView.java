package org.swdc.websdk.views.requests;

import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.MultiChangeBuilder;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.annotations.MultipleImplement;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.fx.FXResources;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.views.LanguageKeys;
import org.swdc.websdk.views.ResponseBodyView;
import org.swdc.websdk.views.events.ContentChangeEvent;

import java.time.Duration;
import java.util.ResourceBundle;

@MultipleImplement(ResponseBodyView.class)
public class ResponseBodyJsonView implements ResponseBodyView, EventEmitter {


    private HttpEndpoint endpoint;

    private VirtualizedScrollPane<CodeArea> scrollPane;

    private CodeArea codeArea;

    private HighlightHelper highlightHelper;

    private Events events;

    @Inject
    private FXResources resources;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    private boolean resetLock = false;

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
                    endpoint.getResponseBodyRaw().put(getClass(),codeArea.getText());
                    emit(new ContentChangeEvent());
                }
            });

            codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.TAB) {
                    event.consume();
                    int startParagraph = codeArea.getCaretSelectionBind().getStartParagraphIndex();
                    int endParagraph = codeArea.getCaretSelectionBind().getEndParagraphIndex();
                    MultiChangeBuilder builder = codeArea.createMultiChange();
                    for (int index  = startParagraph; index <= endParagraph; index++) {
                        if (event.isShiftDown()) {
                            String lineText = codeArea.getParagraph(index).getText();
                            if (lineText.startsWith(" ") || lineText.startsWith("\t")) {
                                builder.deleteText(index,0,index,1);
                            }
                        } else {
                            builder.insertText(index, 0," ");
                        }
                    }
                    builder.commit();
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
            String text = codeArea.getText();
            text = text.replace("\t", " ");
            codeArea.replace(0, text.length(),text,(String) null);
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
    public void refreshData(HttpEndpoint endpoint) {
        if (codeArea == null) {
            getView();
        }
        this.endpoint = endpoint;

        resetLock = true;
        this.codeArea.clear();
        String raw = this.endpoint.getResponseBodyRaw().get(getClass());
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
