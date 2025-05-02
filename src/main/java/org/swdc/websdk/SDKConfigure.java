package org.swdc.websdk;

import org.swdc.config.annotations.ConfigureSource;
import org.swdc.config.annotations.Property;
import org.swdc.config.configs.JsonConfigHandler;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.config.PropEditor;
import org.swdc.fx.config.editors.CheckEditor;

@ConfigureSource(value = "assets/config.json",handler = JsonConfigHandler.class)
public class SDKConfigure extends ApplicationConfig {


}
