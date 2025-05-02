package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.generator.DataDescriptor;

import java.util.*;

public class JsonDescriptorGenerator extends AbstractorDescriptorGenerator<JsonNode> {



    private String basePackageName;

    public JsonDescriptorGenerator(String basePackageName) {

        this.basePackageName = basePackageName;

    }


    @Override
    public List<DataDescriptor> generateClasses(String requestClassName, String responseClassName, HttpEndpoints set, HttpEndpoint endpoint, JsonNode node) {
        return doGenerateClasses(requestClassName,responseClassName,set,endpoint,node,false);
    }

    public List<DataDescriptor> doGenerateClasses(String requestClassName, String responseClassName, HttpEndpoints set, HttpEndpoint endpoint, JsonNode node, boolean isDataClass) {

        List<DataDescriptor> descriptors = new ArrayList<>();

        DataDescriptor dds = new DataDescriptor();
        dds.setClassName(requestClassName);

        if (node.isArray()) {

            ArrayNode arrayNode = (ArrayNode)node;
            JsonNode itemNode = arrayNode.get(0);
            String itemType = getTypeClassName(itemNode);
            if (itemType != null){
                dds.setItemClassName(itemType);
            } else {
                List<DataDescriptor> ds = doGenerateClasses(requestClassName + "Item", responseClassName,set,endpoint,itemNode,true);
                descriptors.addAll(ds);
                dds.setItemClassName(requestClassName + "Item");
            }
            dds.setArray(true);
            dds.setClassName(requestClassName);
            dds.setTargetClassName(responseClassName);
            postGenerateDataClass(basePackageName,dds,set,endpoint);
            dds.setSender(!isDataClass);
            descriptors.add(dds);

            return descriptors;
        }

        if (!node.isObject()) {
            postGenerateDataClass(basePackageName,dds,set,endpoint);
            dds.setTargetClassName(responseClassName);
            dds.setSender(!isDataClass);
            return Arrays.asList(dds);
        }

        Iterator<String> fieldNames = node.fieldNames();
        String key = null;
        while (fieldNames.hasNext()) {

            key = fieldNames.next();

            JsonNode item = node.get(key);
            if (item == null) {
                continue;
            }

            String fieldClassName = getTypeClassName(item);
            if (fieldClassName == null) {
                if (item.isObject()) {

                    String subClassName = requestClassName + key.substring(0,1).toUpperCase() + key.substring(1);
                    List<DataDescriptor> rs = doGenerateClasses(subClassName,responseClassName,set,endpoint,item,true);
                    descriptors.addAll(rs);

                    fieldClassName = subClassName;

                } else if (item.isArray()) {
                    ArrayNode nodes = (ArrayNode)item;
                    if (nodes.isEmpty()) {
                        continue;
                    }

                    JsonNode arrItem = item.get(0);
                    String itemClassName = getTypeClassName(arrItem);

                    if (itemClassName == null) {

                        String subClassName = requestClassName + key.substring(0,1).toUpperCase() + key.substring(1) + "Item";
                        List<DataDescriptor> subDs = doGenerateClasses(subClassName,responseClassName,set,endpoint,arrItem,true);
                        descriptors.addAll(subDs);

                        fieldClassName = "List<" + subClassName + ">";
                    }
                }
            }

            if (fieldClassName == null) {
                continue;
            }

            dds.addDataField(key,fieldClassName);

        }

        dds.setTargetClassName(responseClassName);
        dds.setSender(!isDataClass);
        postGenerateDataClass(basePackageName,dds,set,endpoint);
        descriptors.add(dds);

        return descriptors;
    }

    private String getTypeClassName(JsonNode item) {
        if (item.isTextual()) {
            return "String";
        } else if (item.isBoolean()) {
            return "Boolean";
        } else if (item.isInt()) {
            return "Integer";
        } else if (item.isLong()) {
            return "Long";
        } else if (item.isFloat()) {
            return "Float";
        } else if (item.isDouble()) {
            return "Double";
        } else if (item.isShort()) {
            return "Short";
        } else if (item.isBigDecimal()) {
            return "BigDecimal";
        }
        return null;
    }


}
