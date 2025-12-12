package org.swdc.websdk.core.generator;

import org.swdc.websdk.core.HttpEndpoint;

import java.util.*;
import java.util.stream.Collectors;

public class GenerateContext {

    /**
     * 存储所有生成的类描述信息。
     */
    private List<DataClassDescriptor> classDescriptors = new ArrayList<>();

    /**
     * 存储需要重写的类描述信息
     */
    private Map<DataClassDescriptor,DataClassDescriptor> rewroted = new HashMap<>();

    /**
     * 存储Http请求 -> 类描述信息映射，用于记录每个HTTP请求对应的类描述。
     * 一个Http请求只有一个类描述与之对应。
     */
    private Map<HttpEndpoint, DataClassDescriptor> request = new HashMap<>();

    /**
     * 存储Http响应 -> 类描述信息映射，用于记录每个HTTP响应对应的类描述。
     * 一个Http响应只有一个类描述与之对应。
     */
    private Map<HttpEndpoint, DataClassDescriptor> response = new HashMap<>();

    /**
     * 添加一个新的DataClassDescriptor，如果存在结构相同的，则替换为级别最低的。
     * @param classDescriptor 要添加的类描述
     */
    public void addClassDescriptor(HttpEndpoint endpoint, DataClassDescriptor classDescriptor) {

        List<DataClassDescriptor> structureEquals = classDescriptors.stream()
                .filter( cd -> cd.isStructureEquals(classDescriptor))
                .collect(Collectors.toList());

        structureEquals.add(classDescriptor);
        classDescriptors.removeAll(structureEquals);
        DataClassDescriptor minLevelCd = null;
        for (DataClassDescriptor cd : structureEquals) {
            if (minLevelCd == null) {
                minLevelCd = cd;
                continue;
            }
            if (cd.getLevel() < minLevelCd.getLevel()) {
                minLevelCd = cd;
            } else if (cd.getLevel() == minLevelCd.getLevel()) {
                if (cd.getClassName().length() < minLevelCd.getClassName().length()) {
                    minLevelCd = cd;
                }
            }
        }

        structureEquals.remove(minLevelCd);
        for (DataClassDescriptor cd : rewroted.keySet()) {
            DataClassDescriptor target = rewroted.get(cd);
            if (structureEquals.contains(target)) {
                rewroted.put(cd, minLevelCd);
            }
        }

        for (DataClassDescriptor cd : structureEquals) {
            rewroted.put(cd, minLevelCd);
        }

        classDescriptors.add(minLevelCd);
        if (minLevelCd.getLevel() == 0) {
            if (minLevelCd.isSender()) {
                request.put(endpoint, minLevelCd);
            } else {
                response.put(endpoint, minLevelCd);
            }
        }

    }

    private void doRewrite() {
        if (rewroted.isEmpty()) {
            return;
        }

        for (HttpEndpoint endpoint : request.keySet()) {
            if (rewroted.containsKey(request.get(endpoint))) {
                request.put(endpoint, rewroted.get(request.get(endpoint)));
            }
        }

        for (HttpEndpoint endpoint : response.keySet()) {
            if (rewroted.containsKey(response.get(endpoint))) {
                response.put(endpoint, rewroted.get(response.get(endpoint)));
            }
        }

        for(DataClassDescriptor cd : classDescriptors) {

            for(FieldDescriptor fd : cd.getFields()) {
                if (fd.getReference() != null && rewroted.containsKey(fd.getReference()) ) {
                    fd.setReference(rewroted.get(fd.getReference()));
                }
            }

            if (cd.getArrayItemReference() != null && rewroted.containsKey(cd.getArrayItemReference())) {
                cd.setArrayItemReference(rewroted.get(cd.getArrayItemReference()));
            }


        }
        rewroted.clear();
    }

    public DataClassDescriptor getRequest(HttpEndpoint endpoint) {
        doRewrite();
        return request.get(endpoint);
    }

    public DataClassDescriptor getResponse(HttpEndpoint endpoint) {
        doRewrite();
        return response.get(endpoint);
    }

    public List<DataClassDescriptor> getClassDescriptors() {
        doRewrite();
        return Collections.unmodifiableList(classDescriptors);
    }

}
