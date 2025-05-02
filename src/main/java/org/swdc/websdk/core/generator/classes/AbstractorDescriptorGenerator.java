package org.swdc.websdk.core.generator.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.HttpClient;
import org.swdc.websdk.core.*;
import org.swdc.websdk.core.generator.DataDescriptor;
import org.swdc.websdk.views.requests.RequestJsonBodyView;
import org.swdc.websdk.views.requests.RequestUrlEncodedBodyView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractorDescriptorGenerator<T> {

    abstract List<DataDescriptor> generateClasses(String requestClassName,String responseClassName, HttpEndpoints set, HttpEndpoint endpoint, T node);


    protected void postGenerateDataClass(String basePackageName, DataDescriptor dds, HttpEndpoints endpointSet, HttpEndpoint endpoint) {


        List<HttpQueryString> queryStrings = endpoint.getQueryStrings();
        for (HttpQueryString queryString: queryStrings) {
            dds.addParamField(queryString.getParameter(),queryString.getType().getSimpleName());
        }

        List<HttpPathVar> pathVars = endpoint.getPathVars();
        for (HttpPathVar pathVar: pathVars) {
            dds.addParamField(pathVar.getName(),pathVar.getType().getSimpleName());
        }

        dds.addImport(JsonIgnore.class);
        dds.addImport(JsonProperty.class);
        dds.addImport(JsonInclude.class);
        dds.addImport(List.class);
        dds.addImport(Map.class);
        dds.addImport(HttpClient.class);
        dds.setPackageName(basePackageName + "." + endpointSet.getName());
        dds.setBasePackageName(basePackageName);
        dds.setTargetUrl(endpoint.getUrl());
        dds.setMethod(endpoint.getMethod().name());

        if (dds.isArray()) {
            dds.addImport(ArrayList.class);
            dds.setSuperClass("ArrayList<" + dds.getItemClassName() + ">");
        }

        String raw = endpoint.getRequestBodyRaw().get(endpoint.getRequestBodyView());
        if (raw != null && !raw.isBlank()) {
            if (endpoint.getRequestBodyView() == RequestJsonBodyView.class) {
                dds.setContentType("json");
            } else if (endpoint.getRequestBodyView() == RequestUrlEncodedBodyView.class) {
                dds.setContentType("form-url-encoded");
            }
        }

    }

}
