package org.swdc.websdk.core.generator;

import org.swdc.websdk.core.generator.java.JavaSDKTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EndPointSetIntegrateDescriptor extends EndPointSetDescriptor {

    private List<String> importList = new ArrayList<>();

    private List<DataDescriptor> subClasses = new ArrayList<>();

    private List<String> requestors = new ArrayList<>();

    public EndPointSetIntegrateDescriptor(String name, String basePackageName) {
        super(name, basePackageName);
    }

    public void addRequestor(DataDescriptor descriptor) {
        for (String importItem : descriptor.getImportList()) {
            if (!importList.contains(importItem)) {
                importList.add(importItem);
            }
        }
        subClasses.add(descriptor);
    }

    public List<String> getImportList() {
        return importList;
    }

    public List<String> getRequestors() {
        return requestors;
    }

    @Override
    public String generateSetClass(JavaSDKTemplate template) {

        requestors.clear();
        for (DataDescriptor dd : subClasses) {
            String result = dd.buildInnerRequestClass(template);
            requestors.add(result);
        }

        return template.render("IntegrationClientSet.ftl",this);

    }
}
