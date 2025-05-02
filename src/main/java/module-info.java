module swdc.sdkdev {

    requires swdc.application.fx;
    requires swdc.application.dependency;
    requires swdc.application.configs;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires freemarker;
    requires org.fxmisc.richtext;
    requires org.fxmisc.undo;
    requires reactfx;
    requires org.fxmisc.flowless;
    requires swagger.parser.v3;

    requires java.compiler;
    requires swagger.parser.core;
    requires io.swagger.v3.oas.models;
    requires swdc.commons;
    requires org.apache.httpcomponents.httpclient;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires hanlp.portable;

    opens org.swdc.websdk.views to
            javafx.graphics,
            javafx.fxml,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.websdk to
            javafx.graphics,
            javafx.fxml,
            swdc.application.configs,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.websdk.core to
            javafx.base,
            com.fasterxml.jackson.databind,
            com.fasterxml.jackson.core;

    opens org.swdc.websdk.core.generator.java to
            freemarker,
            swdc.application.dependency;

    opens org.swdc.websdk.views.cells to
            javafx.fxml, javafx.graphics,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.websdk.views.requests to
            javafx.fxml,
            javafx.graphics,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.websdk.core.generator to
            freemarker;

    opens org.swdc.websdk.core.generator.classes to
            freemarker;

    opens org.swdc.websdk.views.events to
            javafx.fxml,
            javafx.graphics,
            swdc.application.dependency,
            swdc.application.fx;

    opens icons;
    opens views.main;
    opens views.modal;
    opens lang;


}