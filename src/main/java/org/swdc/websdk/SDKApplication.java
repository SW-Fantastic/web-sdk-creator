package org.swdc.websdk;

import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXApplication;
import org.swdc.fx.SWFXApplication;
import org.swdc.websdk.views.SDKMainView;
import org.swdc.websdk.views.WebEndpointView;

@SWFXApplication(
        splash = SplashView.class,
        configs = SDKConfigure.class,
        assetsFolder = "./assets",
        icons = { "16.png","24.png","32.png","64.png","128.png","256.png" }
)
public class SDKApplication extends FXApplication {

    @Override
    public void onStarted(DependencyContext dependencyContext) {
        SDKMainView mainView = dependencyContext.getByClass(SDKMainView.class);
        mainView.show();
    }

}
