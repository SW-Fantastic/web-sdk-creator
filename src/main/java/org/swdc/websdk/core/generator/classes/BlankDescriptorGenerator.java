package org.swdc.websdk.core.generator.classes;

import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.generator.DataDescriptor;

import java.util.Arrays;
import java.util.List;

public class BlankDescriptorGenerator extends AbstractorDescriptorGenerator<Void> {

    private String basePackageName;

    public BlankDescriptorGenerator(String basePackageName) {
        this.basePackageName = basePackageName;
    }

    @Override
    public List<DataDescriptor> generateClasses(String requestClassName, String responseClassName, HttpEndpoints set, HttpEndpoint endpoint, Void node) {
        DataDescriptor dds = new DataDescriptor();
        dds.setClassName(requestClassName);
        postGenerateDataClass(basePackageName,dds,set,endpoint);
        dds.setTargetClassName(responseClassName);
        dds.setSender(true);
        return Arrays.asList(dds);
    }

}
