package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuButton;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.font.MaterialIconsService;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.core.HttpEndpoints;

@View(viewLocation = "views/main/EndpointList.fxml",stage = false,multiple = true)
public class EndpointListView extends AbstractView {


    @Inject
    private MaterialIconsService materialIconsService;

    @PostConstruct
    public void init() {
        initButton(findById("actions"),"more_horiz");
        initButton(findById("remove"),"delete");
        initButton(findById("add"), "add");
    }

    public void setEndpoints(HttpEndpoints endpoints) {
        EndpointListController controller = getController();
        controller.setEndPoints(endpoints);
    }

    private void initButton(ButtonBase button, String icon) {
        button.setPadding(new Insets(4));
        button.setFont(materialIconsService.getFont(FontSize.MIDDLE_SMALL));
        button.setText(materialIconsService.getFontIcon(icon));
    }

}
