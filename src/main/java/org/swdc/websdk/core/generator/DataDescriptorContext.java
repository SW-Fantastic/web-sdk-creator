package org.swdc.websdk.core.generator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataDescriptorContext {

    /**
     * 该部分描述符生成Response类
     */
    private Map<String, DataDescriptor> responseDescriptor = new HashMap<>();

    /**
     * 该部分描述符生成Request所依赖的数据类型，不用于发送请求，而是承载数据
     */
    private Map<String, DataDescriptor> requestDataDescriptor = new HashMap<>();

    /**
     * 该部分描述生成Request类，并且该部分类型用于发送请求。
     */
    private Map<String, DataDescriptor> requestsDescriptor = new HashMap<>();

    private boolean miniumMode;

    private Map<String,String> reuseAs = new HashMap<>();

    public DataDescriptor addRequest(DataDescriptor dataDescriptor) {
        requestsDescriptor.put(dataDescriptor.getClassName(),dataDescriptor);
        return dataDescriptor;
    }

    public DataDescriptor addRequestData(DataDescriptor dataDescriptor) {
        return compareAndCache(dataDescriptor,requestDataDescriptor);
    }

    public DataDescriptor addResponse(DataDescriptor dataDescriptor) {
        return compareAndCache(dataDescriptor,responseDescriptor);
    }


    public void setMiniumMode(boolean miniumMode) {
        this.miniumMode = miniumMode;
    }

    public boolean isMiniumMode() {
        return miniumMode;
    }

    public Collection<DataDescriptor> getRequests() {

        return Stream.of(requestsDescriptor.values(),requestDataDescriptor.values())
                .flatMap(Collection::stream)
                .peek(dds -> {
                    if (reuseAs.containsKey(dds.getTargetClassName())) {
                        dds.setTargetClassName(
                                reuseAs.get(dds.getTargetClassName())
                        );
                    }
                    Map<String,String> rewriteFields = new HashMap<>();
                    for (Map.Entry<String,String> fields : dds.getFieldClassMap().entrySet()) {
                        String value = fields.getValue();
                        String rewriteTemplate = "%s";
                        if (value.contains("<")) {
                            rewriteTemplate = value.substring(0,value.indexOf("<")) + "<%s>" + value.substring(value.lastIndexOf(">") + 1);
                            value = value.substring(value.indexOf("<") + 1, value.lastIndexOf(">"));
                        }

                        if (reuseAs.containsKey(value)) {
                            rewriteFields.put(fields.getKey(),String.format(rewriteTemplate,reuseAs.get(value)));
                        }
                    }
                    dds.getFieldClassMap().putAll(rewriteFields);
                })
                .filter(dds -> !isMiniumMode() || !dds.isSender())
                .collect(Collectors.toList());
    }

    public Collection<DataDescriptor> getResponses() {

        for (DataDescriptor dds : responseDescriptor.values()) {

            Map<String,String> rewriteFields = new HashMap<>();
            for (Map.Entry<String,String> fields : dds.getFieldClassMap().entrySet()) {
                String value = fields.getValue();
                String rewriteTemplate = "%s";
                if (value.contains("<")) {
                    rewriteTemplate = value.substring(0,value.indexOf("<")) + "<%s>" + value.substring(value.lastIndexOf(">") + 1);
                    value = value.substring(value.indexOf("<") + 1, value.lastIndexOf(">"));
                }

                if (reuseAs.containsKey(value)) {
                    rewriteFields.put(fields.getKey(),String.format(rewriteTemplate,reuseAs.get(value)));
                }

            }
            dds.getFieldClassMap().putAll(rewriteFields);

            if (dds.isArray()) {
                String itemClass = getReusableType(dds.getItemClassName());
                dds.setSuperClass("ArrayList<" + itemClass + ">");
            }
        }

        return responseDescriptor.values();
    }

    private DataDescriptor compareAndCache(DataDescriptor dataDescriptor, Map<String,DataDescriptor> descriptors) {

        for (Map.Entry<String,DataDescriptor> entry : descriptors.entrySet()) {

            DataDescriptor dds = entry.getValue();

            if (dds.getFieldClassMap().size() != dataDescriptor.getFieldClassMap().size()) {
                continue;
            }

            boolean matched = true;
            Map<String,String> fieldMap = dataDescriptor.getFieldClassMap();
            for (Map.Entry<String,String> fieldEnt: dds.getFieldClassMap().entrySet()) {

                String key = fieldEnt.getKey();

                if (!fieldMap.containsKey(key)) {
                    matched = false;
                    break;
                }

                String val = getReusableType(fieldEnt.getValue());
                String targetType = getReusableType(fieldMap.get(key));
                if (!targetType.equals(val)) {
                    matched = false;
                    break;
                }

            }

            if (matched) {
                reuseAs.put(dataDescriptor.getClassName(),dds.getClassName());
                return dds;
            }

        }

        descriptors.put(dataDescriptor.getClassName(),dataDescriptor);
        return dataDescriptor;
    }


    public String getReusableType(String val) {
        if (val.contains("<")) {
            String rewriteTemplate = val.substring(0,val.indexOf("<")) + "<%s>" + val.substring(val.lastIndexOf(">") + 1);
            val = val.substring(val.indexOf("<") + 1, val.lastIndexOf(">"));
            if (reuseAs.containsKey(val)) {
                val = String.format(rewriteTemplate,val);
            }
        } else if (reuseAs.containsKey(val)) {
            val = reuseAs.get(val);
        }
        return val;
    }



}
