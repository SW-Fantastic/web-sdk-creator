package org.swdc.websdk.core.generator;


import org.swdc.websdk.core.generator.java.JavaSDKTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDescriptor {

    /**
     * 类名
     */
    private String className;

    /**
     * Response类型的名称
     */
    private String targetClassName;

    /**
     * 是否生成为数组类型
     */
    private boolean array;

    /**
     * 数组的Item的类型
     */
    private String itemClassName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 父类，如果需要的话
     */
    private String superClass;

    /**
     * 字段的Json属性 - 字段名称的map
     */
    private Map<String,String> fieldNameMap = new HashMap<>();

    /**
     * 字段的Json属性 - 字段的Java类型的map
     */
    private Map<String,String> fieldClassMap = new HashMap<>();

    /**
     * 参数字段的属性名 - 字段的Java类型的map
     */
    private Map<String,String> fieldParamMap = new HashMap<>();

    /**
     * Http头
     */
    private Map<String,String> webHeaders = new HashMap<>();


    /**
     * 导入列表
     */
    private List<String> importList = new ArrayList<>();

    /**
     * Request的URL
     */
    private String targetUrl;

    /**
     * 基础包名，需要它引用特定的Java类
     */
    private String basePackageName;

    /**
     * ContentType，用于Request，提供给client用于发送请求
     */
    private String contentType;

    /**
     * Http动词，POST/GET/DELETE/PUT等。
     */
    private String method;

    /**
     * 是否生成send方法。
     */
    private boolean sender;

    public DataDescriptor() {

    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public String getItemClassName() {
        return itemClassName;
    }

    public void setItemClassName(String itemClassName) {
        this.itemClassName = itemClassName;
    }

    public boolean isSender() {
        return sender;
    }

    public void setSender(boolean sender) {
        this.sender = sender;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setBasePackageName(String basePackageName) {
        this.basePackageName = basePackageName;
    }


    public String getBasePackageName() {
        return basePackageName;
    }

    public void addImport(Class clazz) {
        importList.add(clazz.getName());
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public void addDataField(String fieldKey, String type) {
        fieldClassMap.put(fieldKey,type);
        String fieldName =  fieldKey.replaceAll("[^a-zA-Z0-9]", "");
        fieldNameMap.put(fieldKey,fieldName);
    }

    public void addParamField(String field, String type) {
        fieldParamMap.put(field,type);
        String fieldName =  field.replaceAll("[^a-zA-Z0-9]", "");
        fieldNameMap.put(field,fieldName);
    }

    public void addHeader(String key, String value) {
        webHeaders.put(key,value);
        String fieldName =  key.replaceAll("[^a-zA-Z0-9]", "");
        fieldNameMap.put(key,fieldName);
    }

    public Map<String, String> getWebHeaders() {
        return webHeaders;
    }

    public Map<String, String> getFieldClassMap() {
        return fieldClassMap;
    }

    public Map<String, String> getFieldNameMap() {
        return fieldNameMap;
    }

    public Map<String, String> getFieldParamMap() {
        return fieldParamMap;
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSuperClass() {
        return superClass;
    }

    public List<String> getImportList() {
        return importList;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public void setTargetClassName(String targetClassName) {
        this.targetClassName = targetClassName;
    }

    public String buildRequestClass(JavaSDKTemplate template) {
        return template.render("Request.ftl", this);
    }

    public String buildResponseClass(JavaSDKTemplate template) {
        return template.render("Response.ftl", this);
    }

    public String buildInnerRequestClass(JavaSDKTemplate template) {
        return template.render("IntegrationRequest.ftl",this);
    }
}
