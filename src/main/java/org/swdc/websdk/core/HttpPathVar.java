package org.swdc.websdk.core;

public class HttpPathVar {

    private String name;

    private Class type;

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Class type) {
        this.type = type;
    }

}
