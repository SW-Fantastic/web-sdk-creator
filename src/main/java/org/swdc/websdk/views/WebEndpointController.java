package org.swdc.websdk.views;

import jakarta.inject.Inject;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.swdc.fx.view.ViewController;
import org.swdc.websdk.core.*;
import org.swdc.websdk.views.cells.HttpEditableCell;
import org.swdc.websdk.views.cells.HttpTypeCell;
import org.swdc.websdk.views.cells.MethodCell;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WebEndpointController extends ViewController<WebEndpointView> {

    @FXML
    private TextField txtUrl;

    @FXML
    private ComboBox<HttpMethod> cbxMethods;

    @FXML
    private TableView<HttpHeader> headersTable;

    @FXML
    private TableColumn<HttpHeader,String> colHeader;

    @FXML
    private TableColumn<HttpHeader, String> colHeaderVal;

    @FXML
    private TableView<HttpQueryString> queryStrTable;

    @FXML
    private TableColumn<HttpQueryString, String> colQueryStringName;

    @FXML
    private TableColumn<HttpQueryString, Class> colQueryStringType;

    @FXML
    private TableView<HttpPathVar> pathVarTable;

    @FXML
    private TableColumn<HttpPathVar,Class> colPathType;

    @FXML
    private TableColumn<HttpPathVar,String> colPathParam;

    @FXML
    private ComboBox<RequestBodyView> cbxReqBodys;

    @FXML
    private ComboBox<ResponseBodyView> cbxRespBodys;

    @FXML
    private BorderPane reqContent;

    @FXML
    private BorderPane respContent;

    @FXML
    private TextField txtName;

    @Inject
    private List<RequestBodyView> requestBodyViews;

    @Inject
    private List<ResponseBodyView> responseBodyViews;

    private volatile HttpEndpoint endpoint = null;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

        txtUrl.textProperty().addListener(this::urlChanged);
        cbxMethods.setCellFactory(lv -> new MethodCell());
        cbxMethods.getSelectionModel().selectedIndexProperty().addListener(this::methodChange);
        cbxMethods.getItems().addAll(HttpMethod.values());

        cbxReqBodys.getItems().addAll(requestBodyViews);
        cbxReqBodys.getSelectionModel().selectedItemProperty().addListener(this::reqBodyChanged);

        cbxRespBodys.getItems().addAll(responseBodyViews);
        cbxRespBodys.getSelectionModel().selectedItemProperty().addListener(this::respBodyChanged);

        colHeader.setCellFactory(tv -> new HttpEditableCell<>(HttpHeader::setHeader, HttpHeader::getHeader));
        colHeaderVal.setCellFactory(tv -> new HttpEditableCell<>(HttpHeader::setValue, HttpHeader::getValue));

        colQueryStringName.setCellFactory(tv -> new HttpEditableCell<>(HttpQueryString::setParameter,HttpQueryString::getParameter));
        colQueryStringType.setCellFactory(tv -> new HttpTypeCell<>(HttpQueryString::getType,HttpQueryString::setType, new Class[] {

                Integer.class,
                Integer[].class,
                String.class,
                String[].class,
                Double.class,
                Boolean.class

        }));

        colPathParam.setCellFactory(tv -> new HttpEditableCell<>(HttpPathVar::setName,HttpPathVar::getName));
        colPathType.setCellFactory(tv -> new HttpTypeCell<>(HttpPathVar::getType,HttpPathVar::setType, new Class[]{

                Integer.class,
                String.class,
                Boolean.class

        }));

        txtName.textProperty().addListener(this::nameChanged);

    }

    private void nameChanged(Observable observable) {
        if (endpoint != null && txtName.getText() != null && !txtName.getText().equals(endpoint.getName())) {
            endpoint.setName(txtName.getText());
        }
    }

    private void respBodyChanged(Observable observable) {

        if (endpoint == null) {
            return;
        }

        ResponseBodyView view = cbxRespBodys.getSelectionModel().getSelectedItem();
        if (view == null) {
            return;
        }

        Node respBodyView = view.getView();
        view.refreshData(endpoint);
        respContent.setCenter(respBodyView);

        endpoint.setResponseBodyView(view.getClass());

    }

    private void reqBodyChanged(Observable observable) {

        if (endpoint == null) {
            return;
        }

        RequestBodyView view = cbxReqBodys.getSelectionModel().getSelectedItem();
        if (view == null) {
            return;
        }

        Node reqBodyView = view.getView();
        view.refreshData(endpoint);
        reqContent.setCenter(reqBodyView);

        endpoint.setRequestBodyView(view.getClass());

    }

    @FXML
    public void addHeader() {

        if (endpoint == null) {
            return;
        }
        HttpHeader httpHeader = new HttpHeader();
        endpoint.getHeaders().add(httpHeader);
        refreshTable();

    }

    @FXML
    public void removeHeader() {

        HttpHeader httpHeader = headersTable.getSelectionModel().getSelectedItem();
        if (httpHeader == null || endpoint == null) {
            return;
        }

        endpoint.getHeaders().remove(httpHeader);
        refreshTable();
    }

    @FXML
    public void addQueryString() {
        if  (endpoint == null) {
            return;
        }
        HttpQueryString queryString = new HttpQueryString();
        endpoint.getQueryStrings().add(queryString);
        refreshTable();
    }

    @FXML
    public void removeQueryString() {
        HttpQueryString queryString = queryStrTable.getSelectionModel().getSelectedItem();
        if (queryString == null || endpoint == null) {
            return;
        }

        endpoint.getQueryStrings().remove(queryString);
        refreshTable();
    }

    private void methodChange(Observable observable) {
        HttpMethod method = cbxMethods
                .getSelectionModel()
                .getSelectedItem();
        if (endpoint != null) {
            endpoint.setMethod(method);
        }
    }

    public void setEndpoint(HttpEndpoint endpoint) {

        this.endpoint = endpoint;

        txtUrl.setText(endpoint.getUrl());
        txtName.setText(endpoint.getName());
        cbxMethods.getSelectionModel().select(endpoint.getMethod());

        Class reqBodyView = endpoint.getRequestBodyView();
        for (RequestBodyView view: requestBodyViews) {
            if (view.getClass() == reqBodyView) {
                cbxReqBodys.getSelectionModel().select(view);
                break;
            }
        }

        Class respBodyView = endpoint.getResponseBodyView();
        for (ResponseBodyView view : responseBodyViews) {
            if (view.getClass() == respBodyView) {
                cbxRespBodys.getSelectionModel().select(view);
                break;
            }
        }

        endpoint.refreshPathVar();
        refreshTable();
        refreshVarTable();
        reqBodyChanged(null);
        respBodyChanged(null);

    }

    private void refreshTable() {
        ObservableList<HttpHeader> headers = headersTable.getItems();
        headers.clear();
        headers.addAll(endpoint.getHeaders());

        ObservableList<HttpQueryString> queryStrings = queryStrTable.getItems();
        queryStrings.clear();
        queryStrings.addAll(endpoint.getQueryStrings());
    }

    private void refreshVarTable() {

        if (endpoint == null) {
            return;
        }

        List<HttpPathVar> vars = endpoint.getPathVars();
        ObservableList<HttpPathVar> varObservableList = pathVarTable.getItems();
        varObservableList.clear();
        varObservableList.addAll(vars);

    }

    private void urlChanged(Observable observable) {
        if (endpoint != null && !txtUrl.getText().equals(endpoint.getUrl())) {
            endpoint.setUrl(txtUrl.getText());
            endpoint.refreshPathVar();
            refreshVarTable();
        }
    }

    public HttpEndpoint getEndpoint() {
        return endpoint;
    }
}
