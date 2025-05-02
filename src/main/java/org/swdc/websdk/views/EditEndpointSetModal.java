package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;

import java.util.ResourceBundle;

@View(
        viewLocation = "views/modal/AddEndpointSetView.fxml",
        dialog = true,
        resizeable = false,
        title = LanguageKeys.UI_ENDPOINT_SET_NAME,
        multiple = true
)
public class EditEndpointSetModal extends AbstractView {

    private String result;

    private TextField txtName;

    @Inject
    private FXResources resources;

    @PostConstruct
    public void initView() {

        txtName = findById("txtName");
        Button btnSave = findById("btnSave");
        Button btnCancel = findById("btnCancel");

        btnSave.setOnAction(this::onSave);
        btnCancel.setOnAction(this::onCancel);

    }

    @Override
    public void show() {
        throw new RuntimeException("Please use showModal() instead.");
    }

    private void onSave(ActionEvent event) {
        this.result = txtName.getText();
        if (result == null || result.isBlank()) {
            ResourceBundle resourceBundle = resources.getResourceBundle();
            Alert alert = alert(
                    resourceBundle.getString(LanguageKeys.DLG_WARN),
                    resourceBundle.getString(LanguageKeys.DLG_NAME_INVALID),
                    Alert.AlertType.WARNING
            );
            alert.showAndWait();
            return;
        }
        this.hide();
    }

    private void onCancel(ActionEvent event) {
        this.result = null;
        this.hide();
    }

    public String showEditModal(String text) {
        result = text;
        txtName.setText(text);
        super.show();
        return result;
    }

    public String showModal() {
        super.show();
        return result;
    }


}
