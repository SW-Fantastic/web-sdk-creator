package org.swdc.websdk.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SDKProject {

    @JsonIgnore
    private File sourceFile;

    private SimpleBooleanProperty miniumMode = new SimpleBooleanProperty();

    private List<HttpEndpoints> endpoints = new ArrayList<>();

    private SimpleStringProperty packageName = new SimpleStringProperty();

    private SimpleStringProperty projectName = new SimpleStringProperty();

    private SimpleObjectProperty<Class> generatorFactory = new SimpleObjectProperty<>();

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public SimpleStringProperty packageNameProperty() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName.set(packageName);
    }

    public String getPackageName() {
        return packageName.get();
    }

    public List<HttpEndpoints> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<HttpEndpoints> endpoints) {
        this.endpoints = endpoints;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public SimpleStringProperty projectNameProperty() {
        return projectName;
    }

    public SimpleBooleanProperty miniumModeProperty() {
        return miniumMode;
    }

    public void setMiniumMode(boolean miniumMode) {
        this.miniumMode.set(miniumMode);
    }

    public boolean isMiniumMode() {
        return miniumMode.get();
    }

    public SimpleObjectProperty<Class> generatorFactoryProperty() {
        return generatorFactory;
    }

    public Class getGeneratorFactory() {
        return generatorFactory.get();
    }

    public void setGeneratorFactory(Class generatorFactory) {
        this.generatorFactory.set(generatorFactory);
    }
}
