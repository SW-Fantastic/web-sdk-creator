package org.swdc.websdk.core.generator.classes;

import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.generator.DataClassDescriptor;

import java.util.List;

/**
 * 空白的类解析器，用于生成空的类描述。
 * 如果Request或者Response的Body为空，可以使用本解析器。
 */
public class BlankClassParser extends AbstractClassParser<String> {

    @Override
    protected List<DataClassDescriptor> parse(String data, HttpEndpoint endpointSet) {
        DataClassDescriptor descriptor = new DataClassDescriptor();
        descriptor.setClassName(generateClassName(endpointSet.getName()));
        return List.of(descriptor);
    }

}
