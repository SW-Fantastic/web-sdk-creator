package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpMethod;
import org.swdc.websdk.views.cells.MethodCell;

import java.util.ResourceBundle;

@View(
        viewLocation = "views/modal/AddEndpointView.fxml",
        dialog = true,
        title = LanguageKeys.UI_ADD_ENDPOINT,
        resizeable = false,
        multiple = true
)
public class AddEndpointModal extends AbstractView {

    private TextField txtUrl;

    private TextField txtName;

    private ComboBox<HttpMethod> cbxMethods;

    private HttpEndpoint result = null;

    @Inject
    private FXResources resources;

    @PostConstruct
    public void init() {

        txtName = findById("txtName");
        txtUrl = findById("txtUrl");
        cbxMethods = findById("cbxMethods");

        cbxMethods.getItems().addAll(HttpMethod.values());
        cbxMethods.setCellFactory(lv -> new MethodCell());

        Button save = findById("btnSave");
        save.setOnAction(this::onSave);

        Button cancel = findById("btnCancel");
        cancel.setOnAction(this::onCancel);

    }

    @Override
    public void show() {
        throw new RuntimeException("using showCreateDialog instead.");
    }

    public HttpEndpoint showCreateDialog() {
        super.show();
        return result;
    }

    public void onSave(ActionEvent event) {

        String name = txtName.getText();
        String url = txtUrl.getText();
        HttpMethod method = cbxMethods.getSelectionModel().getSelectedItem();

        if (name == null || name.isBlank() || url == null || url.isBlank() || method == null) {
            ResourceBundle bundle = resources.getResourceBundle();
            Alert alert = alert(bundle.getString(
                    LanguageKeys.DLG_WARN),
                    bundle.getString(LanguageKeys.DLG_PARAM_INVALID),
                    Alert.AlertType.WARNING
            );
            alert.showAndWait();
            return;
        }

        result = new HttpEndpoint();
        result.setName(name);
        result.setUrl(url);
        result.setMethod(method);
        result.refreshPathVar();
        this.hide();

    }

    public void onCancel(ActionEvent event) {

        result = null;
        this.hide();

    }

}
