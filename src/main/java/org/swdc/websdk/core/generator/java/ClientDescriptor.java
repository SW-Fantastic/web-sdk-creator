package org.swdc.websdk.core.generator.java;

import java.util.HashMap;
import java.util.Map;

public class ClientDescriptor {

    private String className;

    private String basePackageName;

    private Map<String,String> clientNamedMaps = new HashMap<>();

    public ClientDescriptor(String className,String basePackageName) {
        this.basePackageName = basePackageName;
        this.className = className.substring(0,1).toUpperCase() + className.substring(1) + "Client";
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public String getClassName() {
        return className;
    }

    public void addClientApi(String name, String className) {
        clientNamedMaps.put(name,className);
    }

    public Map<String, String> getClientNamedMaps() {
        return clientNamedMaps;
    }

    public String generateApiClient(JavaSDKTemplate template) {
        return template.render("ClientAPI.ftl", this);
    }
}
