package org.swdc.websdk.views;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.swdc.fx.view.ViewController;
import org.swdc.websdk.SDKConfigure;

@Singleton
public class SDKConfigController extends ViewController<SDKConfigView> {

    @Inject
    private SDKConfigure configure;

    @FXML
    public void saveConfig() {
        try {
            configure.save();
            getView().hide();
        } catch (Exception e) {
            Alert alert = getView().alert("提示","配置保存失败！", Alert.AlertType.ERROR);
            alert.showAndWait();
        }
    }

}
