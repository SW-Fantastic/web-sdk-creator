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


    /**
     * 递归解析JSON数据，生成类描述
     * @param data JSON数据
     * @param className 类名
     * @return 类描述列表
     */
    private List<DataClassDescriptor> doParse(JsonNode data, String className) {

        String name = generateClassName(className);
        Map<String,DataClassDescriptor> result = new HashMap<>();
        doParseJson(data, name,0, result);
        return new ArrayList<>(result.values());

    }

    /**
     * 递归解析JSON数据，生成类描述
     * @param data JSON数据
     * @param typeName 类型名
     * @param level 层级
     * @param result 结果集
     * @return 解析是否成功
     */
    private boolean doParseJson(JsonNode data, String typeName, int level, Map<String,DataClassDescriptor> result) {
        if (data == null || (!data.isObject() && !data.isArray())) {
            // 非对象或数组，直接返回，基本类型应该在处理Object的时候顺便解析掉
            // 本方法只处理复杂类型。
            return false;

        } else if (data.isArray()) {

            // 是数组
            ArrayNode arrayNode = (ArrayNode) data;
            if(arrayNode.isEmpty()) {
                // 没有内容的数组是无法解析的
                // 必须提供一个正确的表达了数据结构的json对象。
                return false;
            }

            // 生成类描述对象。
            DataClassDescriptor descriptor = result.computeIfAbsent(typeName, k -> new DataClassDescriptor());
            descriptor.setClassName(generateClassName(typeName));

            JsonNode item = arrayNode.get(0);
            String type = null;
            if (item.isObject()) {
                // 数组内存放的是Object，解析之。
                type = generateClassName(typeName + "_Item");
                if(!doParseJson(item, type, level + 1, result)) {
                    // 无法解析，直接返回false
                    return false;
                }
                // 完成数组项的解析，设置引用类型。
                descriptor.setArray(true);
                descriptor.setArrayItemReference(result.get(type));
            } else {
                // 数组内存放的是基本类型。
                descriptor.setArray(true);
                descriptor.setArrayItemType(getJsonType(item));
            }

            // 存放到结果Map中，以便后续使用。
            result.put(typeName, descriptor);
            return true;

        } else if (data.isObject()) {

            // 是对象
            DataClassDescriptor descriptor = result.computeIfAbsent(typeName, k -> new DataClassDescriptor());
            descriptor.setClassName(typeName);
            // 遍历属性，生成字段描述
            Set<Map.Entry<String, JsonNode>> props = data.properties();
            for (Map.Entry<String, JsonNode> entry : props) {

                JsonNode value = entry.getValue();
                if (value == null || value.isNull()) {
                    // null值直接忽略，因为不能确定类型，无法生成描述。
                    continue;
                }

                // 生成字段名
                String name = entry.getKey();
                String fieldName = generateFieldName(name);
                String valueTypeName = getJsonType(value);

                // 生成字段描述对象。
                FieldDescriptor fd = new FieldDescriptor();
                fd.setName(fieldName);
                fd.setPropertyName(name);

                if (valueTypeName == null) {
                    if (value.isArray()) {
                        // 数组类型，需要递归解析。
                        ArrayNode array = (ArrayNode) value;
                        if (array.isEmpty()) {
                            // 没有内容的数组是无法解析的
                            continue;
                        }

                        JsonNode item = array.get(0);
                        String type = getJsonType(item);
                        if (type != null) {
                            // 基本类型，直接设置。
                            fd.setCollection(true);
                            fd.setType(type);
                        } else {
                            // 对象类型，递归解析。
                            type = generateClassName(typeName + "_" + fieldName + "_Item");
                            if (!doParseJson(item, type, level + 1, result)) {
                                continue;
                            }
                            fd.setCollection(true);
                            fd.setReference(result.get(type));
                        }

                    } else if (value.isObject()) {
                        // 对象类型，递归解析。
                        String type = generateClassName(typeName + "_" + fieldName);
                        if (!doParseJson(value, type, level + 1, result)) {
                            continue;
                        }
                        fd.setReference(result.get(type));

                    }
                } else {
                    // 基本类型，直接设置。
                    fd.setType(valueTypeName);
                }
                // 添加字段到类描述中
                descriptor.setLevel(level);
                descriptor.getFields().add(fd);
            }

            // 存放到结果Map中，以便后续使用。
            result.put(typeName, descriptor);
            return true;
        }
        return false;
    }

    /**
     * 根据JSON节点类型，返回对应的Java基本数据类型名称
     * @param value JSON节点
     * @return Java基本数据类型名称，如果无法确定则返回null。
     *         通常返回null的时候代表它不是基本类型。
     */
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
