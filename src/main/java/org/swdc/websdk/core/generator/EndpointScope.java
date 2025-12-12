package org.swdc.websdk.core.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EndpointScope {

    private DataClassDescriptor classDescriptor;

    private String basePackageName;

    public EndpointScope(String packageName, DataClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.basePackageName = packageName;
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public List<FieldDescriptor> getFields() {
        return classDescriptor.getFields().stream()
                .filter(field -> !field.isHeader() && !field.isPathVar() && !field.isQueryString())
                .collect(Collectors.toList());
    }

    public List<String> getImports() {
        List<String> imports = new ArrayList<>();
        for (Class c : classDescriptor.getImportClasses()) {
            imports.add(c.getName());
        }
        return imports;
    }

    public String getPackageName() {
        return classDescriptor.getPackageName();
    }

    public String getSuperClass() {
        if (classDescriptor.isArray()) {
            if (classDescriptor.getArrayItemReference() != null) {
                return  "ArrayList<" + classDescriptor.getArrayItemReference().getClassName() + ">";
            }
            return "ArrayList<" + classDescriptor.getArrayItemType() + ">";
        }
        return null;
    }

    public String getClassName() {
        return classDescriptor.getClassName();
    }

}
