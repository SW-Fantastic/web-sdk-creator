package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.client.HttpClient;
import org.swdc.websdk.core.*;
import org.swdc.websdk.core.generator.DataClassDescriptor;
import org.swdc.websdk.core.generator.FieldDescriptor;

import java.util.*;

public class AbstractClassParser<T> {

    private String basePackageName;

    public List<DataClassDescriptor> parse(String basePackageName, HttpEndpoint endpoint, HttpEndpoints httpEndpointSet, boolean sender, T data) {

        this.basePackageName = basePackageName;
        List<DataClassDescriptor> descriptors = parse(data, endpoint);
        for (DataClassDescriptor descriptor : descriptors) {
            descriptor.setSender(sender);
            afterParse(httpEndpointSet,endpoint,descriptor);
        }

        return descriptors;
    }

    protected List<DataClassDescriptor> parse(T data, HttpEndpoint endpointSet) {
        return Collections.emptyList();
    }

    protected void afterParse(HttpEndpoints endpointSet, HttpEndpoint endpoint ,DataClassDescriptor descriptor) {

        List<Class> addedImports = List.of(
                JsonIgnore.class,
                JsonProperty.class,
                JsonInclude.class,
                List.class,
                Map.class,
                HttpClient.class,
                ArrayList.class
        );

        descriptor.setPackageName(basePackageName + "." + endpointSet.getName());
        descriptor.setTargetUrl(endpoint.getUrl());
        descriptor.setHttpMethod(endpoint.getMethod().name());
        descriptor.getImportClasses().addAll(addedImports);

        List<FieldDescriptor> additionalFields = new ArrayList<>();
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
        descriptor.setTargetUrl(endpoint.getUrl());

    }


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
