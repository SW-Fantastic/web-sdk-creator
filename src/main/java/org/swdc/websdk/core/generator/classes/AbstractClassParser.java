package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.client.HttpClient;
import org.swdc.websdk.core.*;
import org.swdc.websdk.core.generator.DataClassDescriptor;
import org.swdc.websdk.core.generator.FieldDescriptor;

import java.util.*;

/**
 * 类解析器，将特定数据结构的声明解析为一个Class描述。
 * @param <T> 数据结构
 */
public class AbstractClassParser<T> {

    /**
     * 基础包名，所有的类型都以此包名为基础。
     */
    private String basePackageName;

    /**
     * 解析数据结构，生成类描述。
     * @param basePackageName 基础包名，所有的类型都以此包名为基础
     * @param endpoint  Http接口定义
     * @param httpEndpointSet Http接口定义集合
     * @param sender 是否用于发送请求
     * @param data 数据结构，可以是多种类型，通过泛型T指定。
     * @return 类描述列表，每个数据结构对应一个类描述。
     */
    public List<DataClassDescriptor> parse(String basePackageName, HttpEndpoint endpoint, HttpEndpoints httpEndpointSet, boolean sender, T data) {

        this.basePackageName = basePackageName;
        List<DataClassDescriptor> descriptors = parse(data, endpoint);
        for (DataClassDescriptor descriptor : descriptors) {
            descriptor.setSender(sender);
            afterParse(httpEndpointSet,endpoint,descriptor);
        }

        return descriptors;
    }

    /**
     * 解析数据结构，生成类描述。
     * 重写此方法实现对泛型T类型的数据结构的解析。
     * @param data 数据结构，可以是多种类型，通过泛型T指定。
     * @param endpointSet Http的API接口集合，包含了很多Http接口定义。
     * @return 类描述列表，每个数据结构对应一个类描述。
     */
    protected List<DataClassDescriptor> parse(T data, HttpEndpoint endpointSet) {
        return Collections.emptyList();
    }

    /**
     * 解析完成后，做一些额外的处理，
     * 追加必要的Web参数字段以及规整类的包名。
     * @param endpointSet Http的API接口集合，包含了很多Http接口定义。
     * @param endpoint 当前正在解析的Http接口定义。
     * @param descriptor 当前正在解析的类描述。
     */
    protected void afterParse(HttpEndpoints endpointSet, HttpEndpoint endpoint ,DataClassDescriptor descriptor) {

        // 追加必要的导入类。
        List<Class> addedImports = List.of(
                JsonIgnore.class,
                JsonProperty.class,
                JsonInclude.class,
                List.class,
                Map.class,
                HttpClient.class,
                ArrayList.class
        );

        // 规整类的包名。
        descriptor.setPackageName(basePackageName + "." + endpointSet.getName());
        // 添加必要的Web参数
        descriptor.setTargetUrl(endpoint.getUrl());
        descriptor.setHttpMethod(endpoint.getMethod().getValue());
        descriptor.getImportClasses().addAll(addedImports);

        List<FieldDescriptor> additionalFields = new ArrayList<>();
        // 追加Web的QueryString
        List<HttpQueryString> queryStrings = endpoint.getQueryStrings();
        for (HttpQueryString queryString: queryStrings) {

            FieldDescriptor field = new FieldDescriptor();
            field.setName(generateFieldName(queryString.getParameter()));
            field.setQueryString(true);
            field.setType(queryString.getType().getName());
            field.setPropertyName(queryString.getParameter());
            field.setCollection(queryString.getType().isArray());
            additionalFields.add(field);

        }

        // 追加Web的路径变量字段
        List<HttpPathVar> pathVars = endpoint.getPathVars();
        for (HttpPathVar pathVar: pathVars) {
            FieldDescriptor field = new FieldDescriptor();
            field.setName(generateFieldName(pathVar.getName()));
            field.setPathVar(true);
            field.setPropertyName(pathVar.getName());
            field.setType(pathVar.getType().getName());
            field.setCollection(pathVar.getType().isArray());
            additionalFields.add(field);
        }

        // 追加HttpHeader字段
        List<HttpHeader> headers = endpoint.getHeaders();
        for (HttpHeader httpHeader : headers) {
            FieldDescriptor field = new FieldDescriptor();
            field.setName(generateFieldName(httpHeader.getHeader()));
            field.setType(String.class.getName());
            field.setPropertyName(httpHeader.getHeader());
            field.setHeader(true);
            additionalFields.add(field);
        }

        descriptor.getFields().addAll(additionalFields);

    }


    /**
     * 生成类名，将下划线转换为驼峰命名法。
     * @param name 类名或字段名
     * @return 驼峰命名法命名的类名
     */
    protected String generateClassName(String name) {
        if (name.contains("_")) {
            StringBuilder sb = new StringBuilder();
            String[] parts = name.split("_");
            for (String part : parts) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
            return sb.toString();
        }
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    /**
     * 生成字段名，将下划线转换为驼峰命名法。
     * @param name 字段名
     * @return 驼峰命名法命名的字段名
     */
    protected String generateFieldName(String name) {

        String[] parts = null;
        if (name.contains("_")) {
            parts = name.split("_");
        } else if (name.contains("-")) {
            parts = name.split("-");
        }
        if (parts != null && parts.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {

                if (i > 0) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)));
                } else {
                    sb.append(Character.toLowerCase(parts[i].charAt(0)));
                }
                if (parts[i].length() > 1) {
                    sb.append(parts[i].substring(1));
                }
            }
            return sb.toString();
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

}
