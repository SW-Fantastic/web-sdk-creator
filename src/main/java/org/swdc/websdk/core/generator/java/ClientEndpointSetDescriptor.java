package org.swdc.websdk.core.generator.java;

import org.swdc.websdk.core.generator.DataClassDescriptor;

import java.util.ArrayList;
import java.util.List;

public class ClientEndpointSetDescriptor extends ClientSetDescriptor {

    private List<String> importList = new ArrayList<>();

    private List<String> requestors = new ArrayList<>();

    public ClientEndpointSetDescriptor(String name, String basePackageName) {
        super(name, basePackageName);
    }

    public void addImports(DataClassDescriptor descriptor) {
        for (Class importItem : descriptor.getImportClasses()) {
            if (!importList.contains(importItem.getName())) {
                importList.add(importItem.getName());
            }
        }
    }

    public List<String> getImportList() {
        return importList;
    }

    public List<String> getRequestors() {
        return requestors;
    }

    public void addRequestorSource(String source) {
        requestors.add(source);
    }

}
