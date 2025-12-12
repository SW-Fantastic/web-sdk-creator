package org.swdc.websdk.core.generator;

public class FieldDescriptor {

    /**
     * Class名称，只用于表达基本的java类。
     */
    private String type;

    /**
     * Class定义引用，如果是基本类型则为null，
     * 这代表本字段使用的Class是我们解析得到的Class，这个Class是生成的。
     */
    private DataClassDescriptor reference;

    /**
     * 字段名称。
     */
    private String name;

    /**
     * 属性名称，写入WebParam或者JsonProperty
     * 注解的名称，将会被用于构建Web请求。
     */
    private String propertyName;

    /**
     * 是否为集合类型，标记为true将会构建为可变参数。
     */
    private boolean collection;

    /**
     * 是否为查询字符串，标记为true将会构建为WebParam注解。
     */
    private boolean queryString;

    /**
     * 是否为路径变量，标记为true将会构建为WebParam注解。
     */
    private boolean pathVar;

    /**
     * 是否为Header，标记为true将会构建为Header相关的注解。
     */
    private boolean header;

    /**
     * 默认值，用于生成代码时填充字段的默认值。
     */
    private String defaultValue = "";

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    public boolean isQueryString() {
        return queryString;
    }

    public void setQueryString(boolean queryString) {
        this.queryString = queryString;
    }

    public boolean isPathVar() {
        return pathVar;
    }

    public void setPathVar(boolean pathVar) {
        this.pathVar = pathVar;
    }

    public DataClassDescriptor getReference() {
        return reference;
    }

    public void setReference(DataClassDescriptor reference) {
        this.reference = reference;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isHeader() {
        return header;
    }

    public String getSimpleTypeName() {
        String className = null;
        if (reference != null) {
            className = reference.getClassName();
        } else {
            try {
                className = Class.forName(type).getSimpleName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (isCollection()) {
            return "List<" + className + ">";
        }
        return className;

    }

}
