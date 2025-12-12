package org.swdc.websdk.core.generator.classes;

import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.generator.DataClassDescriptor;

import java.util.List;

public class BlankClassParser extends AbstractClassParser<String> {

    @Override
    protected List<DataClassDescriptor> parse(String data, HttpEndpoint endpointSet) {
        DataClassDescriptor descriptor = new DataClassDescriptor();
        descriptor.setClassName(generateClassName(endpointSet.getName()));
        return List.of(descriptor);
    }

}
