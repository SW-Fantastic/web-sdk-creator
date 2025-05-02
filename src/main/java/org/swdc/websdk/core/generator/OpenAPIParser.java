package org.swdc.websdk.core.generator;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.swdc.ours.common.network.ApacheRequester;
import org.swdc.ours.common.network.Methods;
import org.swdc.websdk.core.*;
import org.swdc.websdk.views.requests.*;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

public class OpenAPIParser {


    public OpenAPIParser() {


    }

    public List<HttpEndpoints> parseDefinitionUrl(String url) {

        try {
            ApacheRequester requester = new ApacheRequester();
            InputStream is = requester.execute(Methods.GET,url,new HashMap<>(),null);
            String content = new String(is.readAllBytes());
            return parseDefinition(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<HttpEndpoints> parseDefinition(String openApiDefinitionStr) {

        List<HttpEndpoints> endpointSet = new ArrayList<>();
        ParseOptions options = new ParseOptions();
        options.setResolveFully(true);
        options.setResolve(true);

        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        OpenAPI openAPI = parser.readContents(openApiDefinitionStr,null,options).getOpenAPI();
        Paths paths = openAPI.getPaths();

        int index = 0;

        for (String key: paths.keySet()) {

            HttpEndpoints endpoints = new HttpEndpoints();
            endpoints.setName("Api" + index ++);

            PathItem item = paths.get(key);
            Operation getAction = item.getGet();
            if (getAction != null) {
                HttpEndpoint endpoint = parseEndpoint(
                        HttpMethod.GET,key, getAction
                );
                endpoints.getEndpoints().add(endpoint);
            }

            Operation postAction = item.getPost();
            if (postAction != null) {
                HttpEndpoint endpoint = parseEndpoint(
                        HttpMethod.POST,key, postAction
                );
                endpoints.getEndpoints().add(endpoint);
            }

            Operation putAction = item.getPut();
            if (putAction != null) {
                HttpEndpoint endpoint = parseEndpoint(
                        HttpMethod.PUT,key, putAction
                );
                endpoints.getEndpoints().add(endpoint);
            }

            Operation deleteAction = item.getDelete();
            if (deleteAction != null) {
                HttpEndpoint endpoint = parseEndpoint(
                        HttpMethod.DELETE,key, deleteAction
                );
                endpoints.getEndpoints().add(endpoint);
            }

            Operation patch = item.getPatch();
            if (patch != null) {
                HttpEndpoint endpoint = parseEndpoint(
                        HttpMethod.PATCH,key, patch
                );
                endpoints.getEndpoints().add(endpoint);
            }

            endpointSet.add(endpoints);

        }
        return endpointSet;
    }


    private String generateName(String summary) {

        StringBuilder name = new StringBuilder();
        List<Term> terms = StandardTokenizer.segment(summary);
        for (Term term: terms) {
            String text = term.word.substring(0,1).toUpperCase() + term.word.substring(1);
            if (text.isBlank()) {
                continue;
            }
            name.append(text);
        }

        return name.substring(0,1).toLowerCase() + name.substring(1);

    }

    private Class mapClasses(Schema schema) {
        String type = schema.getType();
        if (type.equals("string") || type.equals("String")) {
            return String.class;
        } else if (type.equals("integer") || type.equals("Integer")) {
            return Integer.class;
        } else if (type.equals("boolean") || type.equals("Boolean")) {
            return Boolean.class;
        } else if (type.equals("number") || type.equals("Number")) {
            return Double.class;
        } else if (type.equals("array") || type.equals("Array")) {
            Class target = mapClasses(schema.getItems());
            if (target != null) {
                return Array.newInstance(target).getClass();
            }
        } else if (type.equals("object") || type.equals("Object")) {
            return null;
        }
        throw new RuntimeException("unknown type : " + type);
    }

    private HttpEndpoint parseEndpoint(HttpMethod method, String path, Operation operation) {

        HttpEndpoint endpoint = new HttpEndpoint();
        endpoint.setMethod(method);
        endpoint.setUrl(path);
        endpoint.setName(generateName(operation.getSummary()));

        ObjectMapper mapper = new ObjectMapper();

        for (Parameter parameter: operation.getParameters()) {

            if (parameter instanceof QueryParameter) {

                HttpQueryString string = new HttpQueryString();
                string.setParameter(parameter.getName());
                string.setType(mapClasses(parameter.getSchema()));
                endpoint.getQueryStrings().add(string);

            }

        }

        if (operation.getRequestBody() != null) {
            RequestBody body = operation.getRequestBody();
            Content content = body.getContent();
            for (String key: content.keySet()) {
                if (key.contains("json")) {
                    Schema schema = content.get(key).getSchema();
                    if (schema.getExample() != null) {
                        try {
                            String text = mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(schema.getExample());
                            endpoint.getRequestBodyRaw().put(
                                    RequestJsonBodyView.class,
                                    text
                            );
                            endpoint.setRequestBodyView(RequestJsonBodyView.class);
                        } catch (Exception e) {

                        }

                    } else {
                        endpoint.setRequestBodyView(RequestBlankView.class);
                    }

                } else if (key.contains("x-www-form-urlencoded")) {
                    Schema schema = content.get(key).getSchema();
                    Map<String,Schema> props = schema.getProperties();
                    List<HttpBodyEntry> formItems = new ArrayList<>();
                    if(props != null) {
                        HttpBodyEntry entry = new HttpBodyEntry();
                        for (String formKey : props.keySet()) {
                            Schema propSchema = props.get(formKey);
                            entry.setName(formKey);
                            entry.setType(mapClasses(propSchema));
                            formItems.add(entry);
                        }
                        try {
                            String formDataRaw = mapper.writeValueAsString(formItems);
                            endpoint.getResponseBodyRaw().put(RequestUrlEncodedBodyView.class,formDataRaw);
                            endpoint.setResponseBodyView(RequestUrlEncodedBodyView.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } else {
            endpoint.setRequestBodyView(RequestBlankView.class);
        }

        ApiResponse response = operation.getResponses().get("200");
        if (response == null) {
            endpoint.setResponseBodyView(ResponseBodyBlankView.class);
        } else {
            Content content = response.getContent();
            for (String key: content.keySet()) {
                if (key.contains("json")) {
                    MediaType type = content.get(key);
                    Schema schema = type.getSchema();
                    if (schema.getExample() != null) {
                        try {
                            String text = mapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(schema.getExample());
                            endpoint.getResponseBodyRaw().put(
                                    ResponseBodyJsonView.class,
                                    text
                            );
                            endpoint.setResponseBodyView(ResponseBodyJsonView.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        endpoint.setResponseBodyView(ResponseBodyBlankView.class);
                    }
                }
            }
        }

        return endpoint;
    }

}
