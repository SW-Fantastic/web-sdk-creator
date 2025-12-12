package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.generator.DataClassDescriptor;
import org.swdc.websdk.core.generator.FieldDescriptor;

import java.math.BigDecimal;
import java.util.*;

public class JsonClassParser extends AbstractClassParser<JsonNode> {

    @Override
    public List<DataClassDescriptor> parse(JsonNode data, HttpEndpoint endpoint) {

        if (data == null || endpoint == null) {
            return Collections.emptyList();
        }

        return doParse(data, endpoint.getName());

    }


    private List<DataClassDescriptor> doParse(JsonNode data, String className) {

        String name = generateClassName(className);
        Map<String,DataClassDescriptor> result = new HashMap<>();
        doParseJson(data, name,0, result);
        return new ArrayList<>(result.values());

    }

    private boolean doParseJson(JsonNode data, String typeName, int level, Map<String,DataClassDescriptor> result) {
        if (data == null || (!data.isObject() && !data.isArray())) {

            return false;

        } else if (data.isArray()) {

            ArrayNode arrayNode = (ArrayNode) data;
            if(arrayNode.isEmpty()) {
                return false;
            }

            DataClassDescriptor descriptor = result.computeIfAbsent(typeName, k -> new DataClassDescriptor());
            descriptor.setClassName(generateClassName(typeName));

            JsonNode item = arrayNode.get(0);
            String type = null;
            if (item.isObject()) {
                type = generateClassName(typeName + "Item");
                if(!doParseJson(item, type, level + 1, result)) {
                    return false;
                }
                descriptor.setArray(true);
                descriptor.setArrayItemReference(result.get(type));
            } else {
                descriptor.setArray(true);
                descriptor.setArrayItemType(getJsonType(item));
            }

            result.put(typeName, descriptor);
            return true;

        } else if (data.isObject()) {

            DataClassDescriptor descriptor = result.computeIfAbsent(typeName, k -> new DataClassDescriptor());
            descriptor.setClassName(typeName);
            Set<Map.Entry<String, JsonNode>> props = data.properties();
            for (Map.Entry<String, JsonNode> entry : props) {

                JsonNode value = entry.getValue();
                if (value == null || value.isNull()) {
                    continue;
                }

                String name = entry.getKey();
                String fieldName = generateFieldName(name);
                String valueTypeName = getJsonType(value);

                FieldDescriptor fd = new FieldDescriptor();
                fd.setName(fieldName);
                fd.setPropertyName(name);

                if (valueTypeName == null) {
                    if (value.isArray()) {

                        ArrayNode array = (ArrayNode) value;
                        if (array.isEmpty()) {
                            continue;
                        }

                        JsonNode item = array.get(0);
                        String type = getJsonType(item);
                        if (type != null) {
                            fd.setCollection(true);
                            fd.setType(type);
                        } else {
                            type = generateClassName(fieldName + "Item");
                            if (!doParseJson(item, type, level + 1, result)) {
                                continue;
                            }
                            fd.setCollection(true);
                            fd.setReference(result.get(type));
                        }

                    } else if (value.isObject()) {

                        String type = generateClassName(fieldName);
                        if (!doParseJson(value, type, level + 1, result)) {
                            continue;
                        }
                        fd.setReference(result.get(type));

                    }
                } else {
                    fd.setType(valueTypeName);
                }
                descriptor.getFields().add(fd);
                descriptor.setLevel(level);
            }

            result.put(typeName, descriptor);
            return true;
        }
        return false;
    }

    private String getJsonType(JsonNode value) {
        if (value.isTextual()) {
            return String.class.getName();
        } else if (value.isInt()) {
            return Integer.class.getName();
        } else if (value.isDouble()) {
            return Double.class.getName();
        } else if (value.isBoolean()) {
            return Boolean.class.getName();
        } else if (value.isLong()) {
            return Long.class.getName();
        } else if (value.isBigDecimal()) {
            return BigDecimal.class.getName();
        } else if (value.isFloat()) {
            return Float.class.getName();
        } else if (value.isNumber()) {
            return Integer.class.getName();
        } else {
            return null;
        }
    }

}
