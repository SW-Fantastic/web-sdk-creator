package org.swdc.websdk.core.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 该类用于描述一个数据类的结构，包括字段信息、导入的类和包名等。
 */
public class DataClassDescriptor {

    /**
     * 该类对应的API的URL,
     * Response类和生成Pojo可以没有。
     */
    private String targetUrl;

    /**
     * 生成的类名
     */
    private String className;

    /**
     * 该类所在的包名
     */
    private String packageName;

    /**
     * 需要导入的类
     */
    private List<Class> importClasses = new ArrayList<>();

    /**
     * 该类包含的字段信息
     */
    private List<FieldDescriptor> fields = new ArrayList<>();

    /**
     * 是否为API的Request发送方
     */
    private boolean sender;

    /**
     * 该类所在的层级，用于生成嵌套的类结构时使用
     * 该字段用于结构裁剪，结构相同的Class，高leve值的将会被丢弃，从而复用低Level的Class
     * 从而降低整个SDK的复杂度。
     *
     */
    private int level = 0;

    /**
     * 该类对应的HTTP方法，例如GET, POST等。
     * 仅用于API的Request类型（sender = true的时候）
     */
    private String httpMethod;

    /**
     * 该类是否为数组类型，例如List<T>或者T[]等,
     * 仅当Level=0的类型，即请求或者响应本身是数组的时候，该字段才会为true，
     * 此字段为true的时候，通常会继承特定的Collection类型，在Java中就是ArrayList
     */
    private boolean array;

    /**
     * 当本数据类型为数组类型（array = true）的时候，
     * 如果数组存储的是基本类型，则这里为类型的名称。
     */
    private String arrayItemType;

    /**
     * 当本数据类型为数组类型（array = true）的时候，
     * 如果数组存储的是解析得到的数据类型，这里为数据类型的描述符。
     */
    private DataClassDescriptor arrayItemReference;

    public DataClassDescriptor getArrayItemReference() {
        return arrayItemReference;
    }

    public void setArrayItemReference(DataClassDescriptor arrayItemReference) {
        this.arrayItemReference = arrayItemReference;
    }

    public void setArrayItemType(String arrayItemType) {
        this.arrayItemType = arrayItemType;
    }

    public String getArrayItemType() {
        return arrayItemType;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public List<Class> getImportClasses() {
        return importClasses;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescriptor> fields) {
        this.fields = fields;
    }

    public String getClassName() {
        return className;
    }

    public void setImportClasses(List<Class> importClasses) {
        this.importClasses = importClasses;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public boolean isStructureEquals(DataClassDescriptor other) {

        if (this.isArray() != other.isArray()) {
            return false;
        }

        if (this.getArrayItemReference() != other.getArrayItemReference()) {
            return false;
        }

        if (this.getArrayItemType() != null && other.getArrayItemType() != null) {
            if (!this.getArrayItemType().equals(other.getArrayItemType())) {
                return false;
            }
        } else if (this.getArrayItemType() != null || other.getArrayItemType() != null) {
            return false;
        }

        if (this.isSender() != other.isSender()) {
            return false;
        }

        if (this.isSender()) {

            if (this.getHttpMethod() != null && other.getHttpMethod() != null) {
                if (!this.getHttpMethod().equals(other.getHttpMethod())) {
                    return false;
                }
            } else if (this.getHttpMethod() != null || other.getHttpMethod() != null) {
                return false;
            }
            if (this.getTargetUrl() != null && other.getTargetUrl() != null) {
                if (!this.getTargetUrl().equals(other.getTargetUrl())) {
                    return false;
                }
            } else if (this.getTargetUrl() != null || other.getTargetUrl() != null) {
                return false;
            }

        }


        Map<String, FieldDescriptor> fields = new HashMap<>();
        for (FieldDescriptor field : this.getFields()) {
            if (!this.isSender()) {
                if (field.isHeader() || field.isPathVar() || field.isQueryString()) {
                    continue;
                }
            }
            fields.put(getDescriableFieldKey(field), field);
        }

        for (FieldDescriptor field : other.getFields()) {
            if (!this.isSender()) {
                if (field.isHeader() || field.isPathVar() || field.isQueryString()) {
                    continue;
                }
            }
            String key = getDescriableFieldKey(field);
            if (!fields.containsKey(key)) {
                return false;
            }

            FieldDescriptor thisField = fields.get(key);
            if (thisField.getSimpleTypeName() != null && field.getSimpleTypeName() != null) {
                if (!thisField.getSimpleTypeName().equals(field.getSimpleTypeName())) {
                    return false;
                }
            } else if (thisField.getSimpleTypeName() != null || field.getSimpleTypeName() != null) {
                return false;
            } else if (!thisField.getReference().isStructureEquals(field.getReference())) {
                return false;
            }

        }
        return true;
    }


    private String getDescriableFieldKey(FieldDescriptor field) {
        StringBuilder sb = new StringBuilder();
        if (field.isHeader()) {
            sb.append("header-");
        }
        if (field.isPathVar()) {
            sb.append("path-");
        }
        if (field.isQueryString()) {
            sb.append("query-");
        }
        if (field.isCollection()) {
            sb.append("collection-");
        }
        sb.append(field.getName());
        return sb.toString();
    }

}
