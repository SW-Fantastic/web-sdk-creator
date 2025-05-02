package org.swdc.websdk.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.SDKProject;
import org.swdc.websdk.core.generator.GeneratorFactory;
import org.swdc.websdk.core.generator.SDKGenerator;
import org.swdc.websdk.views.events.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SDKMainController extends ViewController<SDKMainView> {

    @FXML
    private Accordion accordion;

    @FXML
    private BorderPane content;

    @FXML
    private TextField txtProjectName;

    @FXML
    private TextField txtPackageName;

    @FXML
    private ComboBox<GeneratorFactory> cbxBuilds;

    @FXML
    private Label lblStatus;

    @FXML
    private CheckBox chkMiniumMode;

    @Inject
    private SDKConfigView configView;

    @Inject
    private WebEndpointView endpointView;

    @Inject
    private Logger logger;

    @Inject
    private FXResources resources;

    @Inject
    private List<GeneratorFactory> generatorFactories;

    @Inject
    private OpenAPIImportView importView;

    private boolean projectEdited = false;

    private SDKProject project;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

        accordion.expandedPaneProperty().addListener(this::activeListChanged);
        content.setCenter(endpointView.getView());
        endpointView.getView().setDisable(true);
        txtProjectName.textProperty().addListener(v -> {
            refreshStageTitle(true);
        });

        cbxBuilds.getItems().addAll(generatorFactories);
        cbxBuilds.getSelectionModel().selectedItemProperty().addListener(this::onBuildChanged);

        newProject();

    }

    private void onBuildChanged(Observable observable,GeneratorFactory old, GeneratorFactory next) {
        project.setGeneratorFactory(next == null ? null : next.getClass());
    }

    @EventListener(type = EndpointChangedEvent.class)
    public void onEndpointChanged(EndpointChangedEvent changedEvent) {
        Platform.runLater(() -> {

            setEndpoint(changedEvent.getMessage());
            endpointView.getView().setDisable(false);

        });
    }

    @EventListener(type = EndpointSetDeleteEvent.class)
    public void onEndpointSetDelete(EndpointSetDeleteEvent event) {
        Platform.runLater(() -> {

            HttpEndpoints endpoints = event.getMessage();
            if (endpoints.getEndpoints().contains(endpointView.getEndpoint())) {
                endpointView.getView().setDisable(true);
            }

            project.getEndpoints().remove(endpoints);
            refreshEndpoints();

            refreshStageTitle(true);

        });
    }

    @EventListener(type = StatusEvent.class)
    public void updateStatus(StatusEvent event) {

        Platform.runLater(() -> {
            if (event.isAlert()) {

                ResourceBundle bundle = resources.getResourceBundle();
                Alert alert = getView().alert(
                        bundle.getString(LanguageKeys.DLG_WARN),
                        event.getText(),
                        Alert.AlertType.WARNING
                );

                alert.showAndWait();
                lblStatus.setText("");

            } else {

                lblStatus.setText(event.getText());

            }
        });

    }

    @EventListener(type = ContentChangeEvent.class)
    public void onProjectContentChanged(ContentChangeEvent event) {

        Platform.runLater(() -> {
            refreshStageTitle(true);
        });

    }

    private void refreshStageTitle(boolean projectChanged) {

        ResourceBundle bundle = resources.getResourceBundle();
        String title = bundle.getString(LanguageKeys.APP_NAME) + " - " + txtProjectName.getText() + (projectChanged ? " *" : "");
        Stage stage = getView().getStage();
        stage.setTitle(title);
        this.projectEdited = projectChanged;

    }

    private void activeListChanged(Observable observable, TitledPane old, TitledPane next) {

        if (next != null) {
            getView().emit(
                    new ListUnselectEvent((HttpEndpoints) next.getUserData())
            );
        }

    }

    private void refreshEndpoints() {
        if (project == null) {
            return;
        }

        accordion.getPanes().clear();
        for (HttpEndpoints pts : project.getEndpoints()) {
            accordion.getPanes().add(getView().createWebListView(pts));
        }
    }


    public void setEndpoint(HttpEndpoint endpoint) {
        endpointView.setEndpoint(endpoint);
    }

    @FXML
    private void addRequestSet() {

        EditEndpointSetModal editEndpointSetModal = getView().getView(EditEndpointSetModal.class);
        String str = editEndpointSetModal.showModal();
        if (str == null || str.isEmpty()) {
            return;
        }
        HttpEndpoints endpoint = new HttpEndpoints();
        endpoint.setName(str);
        project.getEndpoints().add(endpoint);
        refreshEndpoints();

        onProjectContentChanged(null);
    }

    @FXML
    private void saveFile() {

        if (project == null) {
            return;
        }

        ResourceBundle resourceBundle = resources.getResourceBundle();
        File projectFile = project.getSourceFile();
        if (projectFile == null) {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(resourceBundle.getString(LanguageKeys.SAVE));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.sdkproject"));
            projectFile = chooser.showSaveDialog(this.getView().getStage());
            if (projectFile == null) {
                return;
            } else {
                project.setSourceFile(projectFile);
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            FileOutputStream fos = new FileOutputStream(projectFile);
            fos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(project));
            fos.close();
            refreshStageTitle(false);

        } catch (Exception e) {
            ResourceBundle bundle = resources.getResourceBundle();
            Alert alert = getView().alert(bundle.getString(LanguageKeys.DLG_WARN),bundle.getString(LanguageKeys.DLG_UNKNOWN_ERR), Alert.AlertType.ERROR);
            alert.showAndWait();
            logger.error("Unknown exception ", e);
        }
    }

    @FXML
    private void newProject() {

        saveFile();

        project = new SDKProject();
        project.setProjectName("untitled");
        txtProjectName.textProperty().unbind();
        txtProjectName.textProperty().bindBidirectional(project.projectNameProperty());
        txtPackageName.textProperty().unbind();
        txtPackageName.textProperty().bindBidirectional(project.packageNameProperty());
        cbxBuilds.getSelectionModel().clearSelection();
        chkMiniumMode.selectedProperty().unbind();
        chkMiniumMode.selectedProperty().bindBidirectional(project.miniumModeProperty());
        refreshEndpoints();
        endpointView.getView().setDisable(true);

        refreshStageTitle(true);

    }

    @FXML
    private void openProject() {

        ResourceBundle bundle = resources.getResourceBundle();

        FileChooser chooser = new FileChooser();
        chooser.setTitle(bundle.getString(LanguageKeys.OPEN));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Project", "*.sdkproject"));
        File file = chooser.showOpenDialog(this.getView().getStage());
        if (file == null) {
            return;
        }
        try {
            InputStream in = new FileInputStream(file);
            ObjectMapper mapper = new ObjectMapper();
            project = mapper.readValue(in,SDKProject.class);
            project.setSourceFile(file);
            txtProjectName.textProperty().unbind();
            txtProjectName.textProperty().bindBidirectional(project.projectNameProperty());

            txtPackageName.textProperty().unbind();
            txtPackageName.textProperty().bindBidirectional(project.packageNameProperty());
            cbxBuilds.getSelectionModel().clearSelection();

            chkMiniumMode.selectedProperty().unbind();
            chkMiniumMode.selectedProperty().bindBidirectional(project.miniumModeProperty());

            if(project.getGeneratorFactory() != null) {
                for (GeneratorFactory factory : generatorFactories) {
                    if (factory.getClass().equals(project.getGeneratorFactory())) {
                        cbxBuilds.getSelectionModel().select(factory);
                    }
                }
            }

            refreshEndpoints();
            refreshStageTitle(false);

        } catch (Exception e) {
            Alert alert = getView().alert(bundle.getString(LanguageKeys.DLG_WARN),bundle.getString(LanguageKeys.DLG_UNKNOWN_ERR), Alert.AlertType.ERROR);
            alert.showAndWait();
            logger.error("Unknown exception ", e);
        }
    }

    @FXML
    private void showSettings() {
        configView.show();
    }

    @FXML
    private void showImport() {

        List<HttpEndpoints> endpoints = importView.showImport();
        if (endpoints == null ||endpoints.isEmpty()) {
            return;
        }
        project.getEndpoints().addAll(endpoints);
        refreshEndpoints();

    }

    @FXML
    private void buildSDK() {

        if (project == null) {
            return;
        }

        if (project.getProjectName() == null || project.getProjectName().isBlank()) {
            return;
        }

        if (project.getPackageName() == null || project.getPackageName().isBlank()) {
            return;
        }

        if (project.getSourceFile() == null) {
            saveFile();
            if (project.getSourceFile() == null) {
                return;
            }
        }

        GeneratorFactory factory = cbxBuilds.getSelectionModel().getSelectedItem();
        if (factory == null) {
            return;
        }

        BorderPane build = (BorderPane) getView().getView();
        Node center = build.getCenter();
        center.setDisable(true);

        resources.getExecutor().submit(() -> {

            SDKGenerator builder = factory.create();
            builder.generate(resources,project);
            Platform.runLater(() -> {
                center.setDisable(false);
            });

        });

    }

}
