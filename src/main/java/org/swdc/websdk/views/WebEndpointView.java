package org.swdc.websdk.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.MaterialIconsService;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.websdk.core.HttpEndpoint;

@View(viewLocation = "views/main/WebEndpointView.fxml", stage = false)
public class WebEndpointView extends AbstractView {

    @Inject
    private MaterialIconsService materialIconsService;

    @PostConstruct
    public void init() {
        initButton(findById("headerAdd"),"add");
        initButton(findById("queryAdd"),"add");
        initButton(findById("headerRemove"),"delete");
        initButton(findById("queryRemove"),"delete");
    }

    private void initButton(ButtonBase button, String icon) {
        button.setPadding(new Insets(4));
        button.setFont(materialIconsService.getFont(FontSize.MIDDLE_SMALL));
        button.setText(materialIconsService.getFontIcon(icon));
    }

    public void setEndpoint(HttpEndpoint endpoint) {
        WebEndpointController controller = getController();
        controller.setEndpoint(endpoint);
    }

    public HttpEndpoint getEndpoint() {
        WebEndpointController controller = getController();
        return controller.getEndpoint();
    }

}
