package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.swdc.fx.FXResources;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;

@View(viewLocation = "views/main/IDEMainView.fxml", title = LanguageKeys.UI_APP_NAME)
public class SDKMainView extends AbstractView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private FXResources resources;

    @PostConstruct
    public void init() {
        initButton(findById("newProject"),"file",false,LanguageKeys.CREATE);
        initButton(findById("open"),"folder-open",false,LanguageKeys.OPEN);
        initButton(findById("addCollection"), "plus",true,LanguageKeys.ADD);
        initButton(findById("save"),"save",false, LanguageKeys.SAVE);
        initButton(findById("build"),"bolt",true, LanguageKeys.BUILD);
        initButton(findById("import"),"globe-asia",true,LanguageKeys.OPENAPI_IMPORT);
        initButton(findById("setting"),"cog",true,LanguageKeys.CONFIG);

    }

    private void initButton(ButtonBase button, String icon,boolean fill,String tooltip) {
        button.setPadding(new Insets(4));
        button.setFont(fill ? fontawsome5Service.getSolidFont(FontSize.VERY_SMALL) : fontawsome5Service.getRegularFont(FontSize.VERY_SMALL));
        button.setText(fontawsome5Service.getFontIcon(icon));
        if (tooltip != null) {
            button.setTooltip(new Tooltip(resources.getResourceBundle().getString(tooltip)));
        }
    }

    public TitledPane createWebListView(HttpEndpoints endpoints) {

        EndpointListView listView = getView(EndpointListView.class);
        VBox lvRoot = (VBox) listView.getView();
        TitledPane titledPane = new TitledPane();
        titledPane.textProperty().bind(endpoints.nameProperty());
        titledPane.setContent(lvRoot);
        listView.setEndpoints(endpoints);

        titledPane.setUserData(endpoints);

        return titledPane;
    }

}
