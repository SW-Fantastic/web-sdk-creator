package org.swdc.websdk.core.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 封装了单个Endpoint的请求和响应信息，
 * 该对象用于渲染FreeMarker的Template，直接参与API的请求相关代码的生成。
 */
public class EndpointRequestScope {

    private String packageName;

    private DataClassDescriptor request;

    private DataClassDescriptor response;

    private String httpMethod;

    private String contentType;


    public EndpointRequestScope(String packageName, String httpMethod, String contentType, DataClassDescriptor request, DataClassDescriptor response) {
        this.packageName = packageName;
        this.request = request;
        this.response = response;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
    }

    /**
     * 获取Content-Type的header。例如：application/json
     * @return Content-Type的header。
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 获取HTTP方法，例如：GET, POST
     * @return
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * 获取父类。
     * @return 父类全限定名，例如：java.util.ArrayList<String>
     */
    public String getSuperClass() {
        if (request.isArray()) {
            if (request.getArrayItemReference() != null) {
                return "ArrayList<" + request.getArrayItemReference().getClassName() + ">";
            }
            return "ArrayList<" + request.getArrayItemType() + ">";
        }
        return null;
    }

    /**
     * 获取基础包名，所有生成的Package和Class都将以这个包名为基础。
     * @return 基础包名
     */
    public String getBasePackageName() {
        return packageName;
    }

    /**
     * 获取包名。
     * @return 包名
     */
    public String getPackageName() {
        return request.getPackageName();
    }

    /**
     * 获取所有导入的类。
     * @return 导入的类列表
     */
    public List<String> getImports() {
        List<String> imports = new ArrayList<>();
        for (Class c : request.getImportClasses()) {
            imports.add(c.getName());
        }
        return imports;
    }

    /**
     * 获取所有RequestBody字段。
     * @return 字段列表
     */
    public List<FieldDescriptor> getRequestFields() {
        return request.getFields().stream()
                .filter(field -> !field.isQueryString() && !field.isPathVar() && !field.isHeader())
                .collect(Collectors.toList());
    }

    /**
     * 获取所有QueryString字段。
     * @return QueryString字段列表
     */
    public List<FieldDescriptor> getWebParams() {
        return request.getFields().stream()
                .filter(FieldDescriptor::isQueryString)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有PathVariable字段。
     * @return PathVariable字段列表
     */
    public List<FieldDescriptor> getPathParams() {
        return request.getFields().stream()
                .filter(FieldDescriptor::isPathVar)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有QueryString和PathVariable字段。
     * @return 字段列表
     */
    public List<FieldDescriptor> getAllParams() {
        return request.getFields().stream()
                .filter(field -> field.isQueryString() || field.isPathVar())
                .collect(Collectors.toList());
    }

    /**
     * 获取所有Header字段。
     * @return Header字段列表
     */
    public List<FieldDescriptor> getHeaders() {
        return request.getFields().stream()
                .filter(FieldDescriptor::isHeader)
                .collect(Collectors.toList());
    }

    /**
     * 获取请求对象的类名。
     * @return 请求对象的类名
     */
    public String getRequestClassName() {
        return request.getClassName();
    }

    /**
     * 获取目标URL，Http请求将会发送到这个URL。
     * @return 目标URL
     */
    public String getTargetUrl() {
        return request.getTargetUrl();
    }

    /**
     * 如果该类型是API的发送对象，并且Level为0，这代表我们将会通过该对象
     * 发送Web请求，此时应该使QueryString，PathVariable等字段发挥作用，
     * 并且生成与发送请求相关的方法。
     *
     * 如果该类型被标记为API的发送对象，且Level不为0，这代表此对象为RequestBody
     * 的内容，此时不应该生成与发送请求相关的方法，并且不应该携带Http相关字段（例如QueryString）。
     *
     * @return true 如果该类型是API的发送对象，并且Level为0
     */
    public boolean isRequestor() {
        return request.isSender() && request.getLevel() == 0;
    }

    /**
     * 获取响应对象的类名。
     * @return 响应对象的类名，如果没有定义则返回"void"。
     */
    public String getResponseClassName() {
        return response == null ? "Void" : response.getClassName();
    }

}
