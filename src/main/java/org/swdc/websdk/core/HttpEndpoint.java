package org.swdc.websdk.core;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.swdc.websdk.views.requests.RequestJsonBodyView;
import org.swdc.websdk.views.requests.ResponseBodyJsonView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpEndpoint {

    private SimpleStringProperty url = new SimpleStringProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleObjectProperty<HttpMethod> method = new SimpleObjectProperty<>(HttpMethod.GET);

    private SimpleObjectProperty<Class> requestBodyView = new SimpleObjectProperty<>(RequestJsonBodyView.class);

    private SimpleObjectProperty<Class> responseBodyView = new SimpleObjectProperty<>(ResponseBodyJsonView.class);

    private List<HttpHeader> headers = new ArrayList<>();

    private List<HttpQueryString> queryStrings = new ArrayList<>();

    private List<HttpPathVar> pathVars = new ArrayList<>();

    private Map<Class,String> requestBodyRaw = new HashMap<>();

    private Map<Class,String> responseBodyRaw = new HashMap<>();


    public HttpMethod getMethod() {
        return method.get();
    }

    public void setMethod(HttpMethod method) {
        this.method.set(method);
    }

    public SimpleObjectProperty<HttpMethod> methodProperty() {
        return method;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public String getUrl() {
        return url.get();
    }

    public void setUrl(String url) {
        this.url.set(url);
    }

    public void refreshPathVar() {
        pathVars.clear();

        String[] parts = getUrl().split("/");
        for (String part: parts) {
            if (part.startsWith("{") && part.endsWith("}")) {
                String name = part.replace("{", "")
                        .replace("}", "");
                HttpPathVar pathVar = new HttpPathVar();
                pathVar.setName(name);
                pathVar.setType(String.class);
                pathVars.add(pathVar);
            }
        }
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HttpHeader> headers) {
        this.headers = headers;
    }

    public List<HttpQueryString> getQueryStrings() {
        return queryStrings;
    }

    public void setQueryStrings(List<HttpQueryString> queryStrings) {
        this.queryStrings = queryStrings;
    }

    public Map<Class, String> getRequestBodyRaw() {
        return requestBodyRaw;
    }

    public void setRequestBodyRaw(Map<Class, String> requestBodyRaw) {
        this.requestBodyRaw = requestBodyRaw;
    }

    public SimpleObjectProperty<Class> requestBodyViewProperty() {
        return requestBodyView;
    }

    public Class getRequestBodyView() {
        return requestBodyView.get();
    }

    public void setRequestBodyView(Class requestBodyView) {
        this.requestBodyView.set(requestBodyView);
    }

    public SimpleObjectProperty<Class> responseBodyViewProperty() {
        return responseBodyView;
    }

    public void setResponseBodyView(Class responseBodyView) {
        this.responseBodyView.set(responseBodyView);
    }

    public Class getResponseBodyView() {
        return responseBodyView.get();
    }

    public Map<Class, String> getResponseBodyRaw() {
        return responseBodyRaw;
    }

    public void setResponseBodyRaw(Map<Class, String> responseBodyRaw) {
        this.responseBodyRaw = responseBodyRaw;
    }

    public List<HttpPathVar> getPathVars() {
        return pathVars;
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode() + getMethod().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpEndpoint) {
            HttpEndpoint endpoint = (HttpEndpoint) obj;
            if(!endpoint.getUrl().equals(this.getUrl())) {
                return false;
            }
            if (endpoint.getName() == null) {
                if (this.getName() != null ){
                    return false;
                }
            } else {
                if (!endpoint.getName().equals(this.getName())) {
                    return false;
                }
            }
            if (!endpoint.getMethod().equals(this.getMethod())) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
