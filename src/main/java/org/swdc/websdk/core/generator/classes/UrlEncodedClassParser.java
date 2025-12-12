package org.swdc.websdk.core.generator.classes;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swdc.websdk.core.HttpBodyEntry;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.generator.DataClassDescriptor;
import org.swdc.websdk.core.generator.FieldDescriptor;

import java.util.Arrays;
import java.util.List;

public class UrlEncodedClassParser extends AbstractClassParser<String> {

    @Override
    protected List<DataClassDescriptor> parse(String data, HttpEndpoint endpoint) {
        ObjectMapper mapper = new ObjectMapper();
        try {

            JavaType type = mapper.getTypeFactory().constructParametricType(
                    List.class, HttpBodyEntry.class
            );

            DataClassDescriptor dds = new DataClassDescriptor();
            dds.setClassName(generateClassName(endpoint.getName()));

            if (data != null && !data.isBlank()) {

                List<HttpBodyEntry> entries = mapper.readValue(data, type);
                for (HttpBodyEntry entry : entries) {
                    String key = endpoint.getName();
                    if (entry.getName().endsWith("[]")) {
                        String name = entry.getName().substring(key.lastIndexOf("]"));
                        FieldDescriptor fd = new FieldDescriptor();
                        fd.setName(generateFieldName(name));
                        fd.setType(entry.getType().getName());
                        fd.setPropertyName(name);
                        fd.setCollection(true);
                        dds.getFields().add(fd);
                    } else {
                        FieldDescriptor fd = new FieldDescriptor();
                        fd.setName(generateFieldName(entry.getName()));
                        fd.setType(entry.getType().getName());
                        fd.setPropertyName(entry.getName());
                        dds.getFields().add(fd);
                    }
                }

            }

            return Arrays.asList(dds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
