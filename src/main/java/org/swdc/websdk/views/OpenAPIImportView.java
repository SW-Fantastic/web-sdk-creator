package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import javafx.beans.Observable;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.views.cells.HttpEndpointItem;

import java.util.List;


@View(viewLocation = "views/modal/ImportOpenAPIView.fxml", dialog = true,title = LanguageKeys.UI_DLG_IMPORT_TITLE)
public class OpenAPIImportView extends AbstractView {


    private ToggleGroup toggleGroup = new ToggleGroup();

    @PostConstruct
    public void initView() {

        ToggleButton togUrl = findById("togUrl");
        ToggleButton togFile = findById("togFile");

        toggleGroup.getToggles().addAll(togUrl,togFile);
        toggleGroup.selectedToggleProperty().addListener(this::togChanged);
        toggleGroup.selectToggle(togUrl);

        getStage().setOnCloseRequest(e -> {
            OpenAPIImportController controller = getController();
            if (!controller.isDoImport()) {
                controller.reset();
            }
        });
    }

    private void togChanged(Observable observable, Toggle old, Toggle next) {

        if (next == null) {
            toggleGroup.selectToggle(old);
            return;
        }

        TextField field = findById("txtUrl");
        field.setText("");
        ToggleButton nextButton = (ToggleButton) next;
        if (nextButton.getId().equals("togFile")) {
            field.setEditable(false);
        } else {
            field.setEditable(true);
        }

    }

    public boolean isUrl() {
        ToggleButton button = (ToggleButton) toggleGroup.getSelectedToggle();
        return button.getId().equals("togUrl");
    }

    @Override
    public void show() {
        throw new UnsupportedOperationException("using showImport instead");
    }

    public List<HttpEndpoints> showImport() {
        super.show();
        OpenAPIImportController controller = getController();
        return controller.getAndReset();
    }

}
