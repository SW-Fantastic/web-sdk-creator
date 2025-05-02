package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;
import org.swdc.fx.FXResources;
import org.swdc.fx.config.ConfigViews;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.SDKConfigure;

@View(
        viewLocation = "views/main/SDKConfigView.fxml",
        title = LanguageKeys.UI_CONFIG,
        resizeable = false
)
public class SDKConfigView extends AbstractView {

    @Inject
    private FXResources resources;

    @Inject
    private SDKConfigure configure;

    private PropertySheet generalConfSheet;

    @PostConstruct
    public void initView(){

        BorderPane root = (BorderPane) getView();

        generalConfSheet = new PropertySheet();
        generalConfSheet.setPropertyEditorFactory(ConfigViews.factory(resources));
        generalConfSheet.setModeSwitcherVisible(false);
        generalConfSheet.setSearchBoxVisible(false);
        generalConfSheet.getStyleClass().add("prop-sheets");
        root.setCenter(generalConfSheet);

        reload();

    }

    public void reload() {

        ObservableList itemsServer = generalConfSheet.getItems();
        ObservableList confServers = ConfigViews.parseConfigs(resources,configure);
        itemsServer.clear();
        itemsServer.addAll(confServers);

    }

}
