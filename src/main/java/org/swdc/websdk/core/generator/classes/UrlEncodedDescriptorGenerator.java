package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swdc.websdk.core.HttpBodyEntry;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.generator.DataDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UrlEncodedDescriptorGenerator extends AbstractorDescriptorGenerator<String> {

    private String basePackageName;

    public UrlEncodedDescriptorGenerator(String basePackageName) {

        this.basePackageName = basePackageName;

    }

    @Override
    public List<DataDescriptor> generateClasses(String requestClassName,String responseClassName, HttpEndpoints set, HttpEndpoint endpoint, String source) {
        ObjectMapper mapper = new ObjectMapper();
        try {

            JavaType type = mapper.getTypeFactory().constructParametricType(
                    List.class, HttpBodyEntry.class
            );

            DataDescriptor dds = new DataDescriptor();
            dds.setClassName(requestClassName);

            if (source != null && !source.isBlank()) {

                List<HttpBodyEntry> entries = mapper.readValue(source, type);
                for (HttpBodyEntry entry : entries) {
                    String key = endpoint.getName();
                    if (entry.getName().endsWith("[]")) {
                        dds.addDataField(entry.getName().substring(key.lastIndexOf("]")), entry.getType().getSimpleName() + "[]");
                    } else {
                        dds.addDataField(entry.getName(), entry.getType().getSimpleName());
                    }
                }

            }

            dds.setTargetClassName(responseClassName);
            postGenerateDataClass(basePackageName,dds,set,endpoint);
            dds.setSender(true);
            return Arrays.asList(dds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
