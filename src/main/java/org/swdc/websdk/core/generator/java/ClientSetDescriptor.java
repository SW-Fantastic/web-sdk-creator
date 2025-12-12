package org.swdc.websdk.core.generator.java;

import java.util.HashMap;
import java.util.Map;

public class ClientSetDescriptor {

    private Map<String,String> requestNamesMap = new HashMap<>();

    private String className;

    private String basePackageName;

    private String name;

    public ClientSetDescriptor(String name, String basePackageName) {

        this.name = name;
        this.className = name.substring(0,1).toUpperCase() + name.substring(1) + "Api";
        this.basePackageName = basePackageName;

    }

    public String getName() {
        return name;
    }

    public void addRequest(String methodName, String className) {
        requestNamesMap.put(methodName,className);
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getRequestNamesMap() {
        return requestNamesMap;
    }

    public String generateSetClass(JavaSDKTemplate template) {
        return template.render("ClientSet.ftl", this);
    }


}
