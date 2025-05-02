package org.swdc.websdk.core;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpEndpoints {

    private SimpleStringProperty name = new SimpleStringProperty();

    private ObservableList<HttpEndpoint> endpoints = FXCollections.observableArrayList();

    public void setEndpoints(List<HttpEndpoint> endpoints) {
        this.endpoints.addAll(endpoints);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<HttpEndpoint> getEndpoints() {
        return endpoints;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HttpEndpoints) {
            HttpEndpoints target = (HttpEndpoints) obj;
            if (!name.equals(target.name)) {
                return false;
            }
            if (endpoints.size() != target.getEndpoints().size()) {
                return false;
            }
            List<String> endpointUrls = endpoints.stream()
                    .map(HttpEndpoint::getUrl)
                    .collect(Collectors.toList());
            for (HttpEndpoint endpoint : target.getEndpoints()) {
                if (!endpointUrls.contains(endpoint.getUrl())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
