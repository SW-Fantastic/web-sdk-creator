package org.swdc.websdk.core.generator.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.EventEmitter;
import org.swdc.fx.FXResources;
import org.swdc.websdk.core.HttpEndpoint;
import org.swdc.websdk.core.HttpEndpoints;
import org.swdc.websdk.core.SDKProject;
import org.swdc.websdk.core.generator.*;
import org.swdc.websdk.core.generator.classes.BlankClassParser;
import org.swdc.websdk.core.generator.classes.JsonClassParser;
import org.swdc.websdk.core.generator.classes.UrlEncodedClassParser;
import org.swdc.websdk.views.LanguageKeys;
import org.swdc.websdk.views.events.StatusEvent;
import org.swdc.websdk.views.requests.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class JavaSDKGenerator implements SDKGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JavaSDKGenerator.class);

    private JavaSDKTemplate template;

    private String classVersion;

    private EventEmitter emitter;

    public JavaSDKGenerator(EventEmitter emitter, JavaSDKTemplate template, String classVersion) {
        this.classVersion = classVersion;
        this.template = template;
        this.emitter = emitter;
    }

    @Override
    public void generate(FXResources resources, SDKProject project) {

        ResourceBundle bundle = resources.getResourceBundle();

        if (project == null || project.getPackageName() == null || project.getPackageName().isBlank() || project.getSourceFile() == null) {
            return;
        }

        File projectFile = project.getSourceFile();

        if (!projectFile.exists()) {
            return;
        }

        String baseDir = project.getPackageName().replaceAll("[.]", "/");
        File projectDir = projectFile.getParentFile();
        File sourceRoot = new File(projectDir.getAbsolutePath(),"src/main/java/" + baseDir);
        if (!sourceRoot.exists()) {
            sourceRoot.mkdirs();
        } else {
            try {
                FileUtil.clean(sourceRoot);
                sourceRoot.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // generate files

        List<File> sourceClasses = new ArrayList<>();

        FileUtil.generateAndWrite(
                template,
                new File(projectDir.getAbsolutePath(),"pom.xml"),
                Map.of("basePackageName", project.getPackageName()),
                "Pom.ftl"
        );

        File webHeaderSource = new File(sourceRoot.getAbsolutePath(),"RequestHeader.java");
        FileUtil.generateAndWrite(
                template,
                webHeaderSource,
                Map.of("basePackageName", project.getPackageName()),
                "RequestHeader.ftl"
        );
        sourceClasses.add(webHeaderSource);

        File webParamSource = new File(sourceRoot.getAbsolutePath(),"WebParam.java");
        FileUtil.generateAndWrite(
                template,
                webParamSource,
                Map.of("basePackageName", project.getPackageName()),
                "WebParam.ftl"
        );
        sourceClasses.add(webParamSource);

        File credentialsSource = new File(sourceRoot.getAbsolutePath(),"WebCredentials.java");
        FileUtil.generateAndWrite(
                template,
                credentialsSource,
                Map.of("basePackageName", project.getPackageName()),
                "WebCredentials.ftl"
        );
        sourceClasses.add(credentialsSource);

        File clientSource = new File(sourceRoot.getAbsolutePath(),"Client.java");
        FileUtil.generateAndWrite(
                template,
                clientSource,
                Map.of("basePackageName", project.getPackageName()),
                "Client.ftl"
        );
        sourceClasses.add(clientSource);

        generateSource(resources, sourceRoot, project, sourceClasses);

    }

    private boolean verifyEndpointSets(SDKProject project, ResourceBundle bundle) {
        for (HttpEndpoints set: project.getEndpoints()) {

            emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.VERIFYING) + set.getName()));
            List<String> methodNames = new ArrayList<>();

            emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.VERIFYING) + set.getName()));
            for (HttpEndpoint endpoint: set.getEndpoints()) {
                if (endpoint.getName() == null || endpoint.getName().isBlank()) {
                    emitter.emit(new StatusEvent(String.format(
                            bundle.getString(LanguageKeys.DLG_VERIFY_NAME_BLANK),endpoint.getUrl()
                    ), 0,true));
                    return false;
                }
                if (endpoint.getUrl() == null || endpoint.getUrl().isBlank()) {
                    emitter.emit(new StatusEvent(String.format(
                            bundle.getString(LanguageKeys.DLG_VERIFY_URL_BLANK),set
                    ), 0,true));
                    return false;
                }
                if (methodNames.contains(endpoint.getName())) {
                    emitter.emit(new StatusEvent(String.format(bundle.getString(LanguageKeys.DLG_VERIFY_NAME_DUPLICATE),endpoint.getName()),0,true));
                    return false;
                }
                methodNames.add(endpoint.getName());
            }
        }
        return true;
    }

    private void generateSource(FXResources resources, File sourceRoot, SDKProject project, List<File> sources) {

        ResourceBundle bundle = resources.getResourceBundle();
        if (!verifyEndpointSets(project, bundle)) {
            return;
        }

        ClientDescriptor clientDescriptor = new ClientDescriptor(
                project.getProjectName(),
                project.getPackageName()
        );

        List<DataClassDescriptor> proceed = new ArrayList<>();
        for (HttpEndpoints endpointSet : project.getEndpoints()) {



            ClientSetDescriptor setDescriptor = new ClientSetDescriptor(endpointSet.getName(), project.getPackageName());
            if (project.isMiniumMode()) {
                setDescriptor = new ClientEndpointSetDescriptor(
                        endpointSet.getName(), project.getPackageName()
                );
            }

            File setPackage = new File(sourceRoot.getAbsolutePath(), endpointSet.getName());
            if (!setPackage.exists()) {
                setPackage.mkdirs();
            }

            GenerateContext context = generateEndpointSet(project, endpointSet);
            for (HttpEndpoint endpoint : endpointSet.getEndpoints()) {

                DataClassDescriptor requestDesc = context.getRequest(endpoint);
                DataClassDescriptor responseDesc = context.getResponse(endpoint);
                if (requestDesc == null) {
                    continue;
                }

                requestDesc.setClassName(requestDesc.getClassName() + "Req");

                Class viewType = endpoint.getRequestBodyView();
                String contentType = null;
                if (viewType == RequestJsonBodyView.class) {
                    contentType = "application/json";
                } else if (viewType == RequestFormDataView.class) {
                    contentType = "application/x-www-form-urlencoded";
                }

                EndpointRequestScope scope = new EndpointRequestScope(
                        project.getPackageName(),
                        endpoint.getMethod().name(),
                        contentType,
                        requestDesc,
                        responseDesc
                );

                if (project.isMiniumMode()) {

                    String requestSource = template.render("IntegrationRequest.ftl", scope);
                    ClientEndpointSetDescriptor integratedSet = (ClientEndpointSetDescriptor) setDescriptor;
                    integratedSet.addRequestorSource(requestSource);
                    integratedSet.addImports(requestDesc);

                } else {

                    String requestSource = template.render("Request.ftl", scope);
                    File target = new File(setPackage.getAbsolutePath(), requestDesc.getClassName() + ".java");
                    try(FileOutputStream fos = new FileOutputStream(target)) {
                        fos.write(requestSource.getBytes(StandardCharsets.UTF_8));
                        sources.add(target);
                    } catch (Exception e) {
                        logger.error("Failed to write file : ", e);
                    }

                }

                proceed.add(requestDesc);
                setDescriptor.addRequest(endpoint.getName(), requestDesc.getClassName());

            }

            List<DataClassDescriptor> classes = context.getClassDescriptors();
            for (DataClassDescriptor desc : classes) {

                if (proceed.contains(desc)) {
                    continue;
                }

                String source = null;
                if (desc.isSender()) {

                    EndpointRequestScope scope = new EndpointRequestScope(
                            project.getPackageName(),
                            null,
                            null,
                            desc,
                            null
                    );
                    source = template.render("Request.ftl", scope);
                    proceed.add(desc);

                } else {

                    EndpointScope scope = new EndpointScope(project.getPackageName(), desc);
                    source = template.render("Response.ftl", scope);
                    proceed.add(desc);

                }

                File target = new File(setPackage.getAbsolutePath(), desc.getClassName() + ".java");
                try(FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(source.getBytes(StandardCharsets.UTF_8));
                    sources.add(target);
                } catch (Exception e) {

                    logger.error("Failed to write file : ", e);
                    emitter.emit(new StatusEvent(
                            String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),target.getAbsolutePath()),
                            0d,true
                    ));
                    return;

                }

            }

            String clientSetSource = null;
            if (project.isMiniumMode()) {
                clientSetSource = template.render("IntegrationClientSet.ftl", setDescriptor);
            } else {
                clientSetSource = template.render("ClientSet.ftl", setDescriptor);
            }
            File descSetClassFile = new File(setPackage.getAbsolutePath(),setDescriptor.getClassName() + ".java");
            try (FileOutputStream fos = new FileOutputStream(descSetClassFile)){
                fos.write(clientSetSource.getBytes(StandardCharsets.UTF_8));
                clientDescriptor.addClientApi(endpointSet.getName(), setDescriptor.getClassName());
                sources.add(descSetClassFile);
            } catch (Exception e) {
                logger.error("Failed to write file : ", e);
                emitter.emit(new StatusEvent(
                        String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),descSetClassFile.getName()),
                        0d,true
                ));
                return;
            }

        }

        File projectClassFile = new File(sourceRoot.getAbsolutePath(),clientDescriptor.getClassName() + ".java");
        try(FileOutputStream fos = new FileOutputStream(projectClassFile)) {
            fos.write(clientDescriptor.generateApiClient(template).getBytes(StandardCharsets.UTF_8));
            sources.add(projectClassFile);
        } catch (Exception e) {
            logger.error("Failed to write file : ", e);
            emitter.emit(new StatusEvent(
                    String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),projectClassFile.getName()),
                    0d,true
            ));
            return;
        }

        if (classVersion != null) {
            emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.PACKAGING)));
            try {
                JavaSDKPackager sdkPackager = new JavaSDKPackager(project,resources.getAssetsFolder(), classVersion);
                sdkPackager.doPackage(sources);
            } catch (Exception e) {
                logger.error("Failed to write file : ", e);
                emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.DLG_COMPILE_FAILED),0,true));
            }
        }

        emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.READY)));
        emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.GENERATED),0,true));
    }


    private GenerateContext generateEndpointSet(SDKProject project, HttpEndpoints endpointSet) {

        GenerateContext context = new GenerateContext();
        JsonClassParser jsonParser = new JsonClassParser();
        BlankClassParser blankParser = new BlankClassParser();
        UrlEncodedClassParser urlEncodedParser = new UrlEncodedClassParser();

        for (HttpEndpoint endpoint : endpointSet.getEndpoints()) {

            // generate request class descriptor
            Class requestViewType = endpoint.getRequestBodyView();
            String req = endpoint.getRequestBodyRaw().get(requestViewType);
            if (requestViewType == null ) {
                continue;
            }

            if (RequestJsonBodyView.class == requestViewType) {
                try {

                    if (req == null || req.isBlank()) {
                        continue;
                    }

                    JsonNode node = mapper.readTree(req);
                    List<DataClassDescriptor> descriptors = jsonParser.parse(
                            project.getPackageName(),endpoint,endpointSet,true,node);
                    for (DataClassDescriptor desc : descriptors) {
                        context.addClassDescriptor(endpoint,desc);
                    }

                } catch (Exception e) {
                    logger.error("Error parsing JSON request for endpoint: " + endpoint.getName(), e);
                }
            } else if (RequestBlankView.class == requestViewType) {

                List<DataClassDescriptor> descriptors = blankParser.parse(
                        project.getPackageName(),endpoint,endpointSet,true,req);
                for (DataClassDescriptor desc : descriptors) {
                    context.addClassDescriptor(endpoint, desc);
                }

            } else if (RequestUrlEncodedBodyView.class == requestViewType) {

                List<DataClassDescriptor> descriptors = urlEncodedParser.parse(
                        project.getPackageName(),endpoint,endpointSet,true,req);
                for (DataClassDescriptor desc : descriptors) {
                    context.addClassDescriptor(endpoint, desc);
                }

            }

        }


        for (HttpEndpoint endpoint : endpointSet.getEndpoints()) {

            // generate response class descriptor
            Class responseViewType = endpoint.getResponseBodyView();
            String resp = endpoint.getResponseBodyRaw().get(responseViewType);
            if (responseViewType == null || responseViewType == ResponseBodyBlankView.class) {
                continue;
            }

            if ( resp == null || resp.isBlank() ) {
                continue;
            }

            if (ResponseBodyJsonView.class == responseViewType) {
                try {
                    JsonNode node = mapper.readTree(resp);
                    List<DataClassDescriptor> descriptors = jsonParser.parse(
                            project.getPackageName(),endpoint,endpointSet,false,node);
                    for (DataClassDescriptor desc : descriptors) {
                        context.addClassDescriptor(endpoint, desc);
                    }

                } catch (Exception e) {
                    logger.error("Error parsing JSON response for endpoint: " + endpoint.getName(), e);
                }
            }

        }
        return context;
    }


}
