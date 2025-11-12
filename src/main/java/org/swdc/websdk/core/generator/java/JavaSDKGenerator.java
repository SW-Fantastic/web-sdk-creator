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
import org.swdc.websdk.core.generator.classes.BlankDescriptorGenerator;
import org.swdc.websdk.core.generator.classes.JsonDescriptorGenerator;
import org.swdc.websdk.core.generator.classes.UrlEncodedDescriptorGenerator;
import org.swdc.websdk.views.LanguageKeys;
import org.swdc.websdk.views.events.StatusEvent;
import org.swdc.websdk.views.requests.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JavaSDKGenerator implements SDKGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JavaSDKGenerator.class);

    private JavaSDKTemplate template;

    private String classVersion;

    private EventEmitter emitter;

    public JavaSDKGenerator(EventEmitter emitter, JavaSDKTemplate template, String classVersion) {
        this.classVersion = classVersion;
        this.template = template;
        this.emitter = emitter;
    }


    public void generate(FXResources resources,SDKProject project) {

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

        ClientDescriptor clientDescriptor = new ClientDescriptor(
                project.getProjectName(),project.getPackageName()
        );


        for (HttpEndpoints set: project.getEndpoints()) {

            List<String> methodNames = new ArrayList<>();

            emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.VERIFYING) + set.getName()));
            for (HttpEndpoint endpoint: set.getEndpoints()) {
                if (endpoint.getName() == null || endpoint.getName().isBlank()) {
                    emitter.emit(new StatusEvent(String.format(
                            bundle.getString(LanguageKeys.DLG_VERIFY_NAME_BLANK),endpoint.getUrl()
                    ), 0,true));
                    return;
                }
                if (endpoint.getUrl() == null || endpoint.getUrl().isBlank()) {
                    emitter.emit(new StatusEvent(String.format(
                            bundle.getString(LanguageKeys.DLG_VERIFY_URL_BLANK),set
                    ), 0,true));
                    return;
                }
                if (methodNames.contains(endpoint.getName())) {
                    emitter.emit(new StatusEvent(String.format(bundle.getString(LanguageKeys.DLG_VERIFY_NAME_DUPLICATE),endpoint.getName()),0,true));
                    return;
                }
                methodNames.add(endpoint.getName());
            }
        }

        for (HttpEndpoints set: project.getEndpoints()) {

            File setPackage = new File(sourceRoot.getAbsolutePath(), set.getName());
            if (!setPackage.exists()) {
                setPackage.mkdirs();
            }


            DataDescriptorContext context = new DataDescriptorContext();

            EndPointSetDescriptor setDescriptor = new EndPointSetDescriptor(set.getName(), project.getPackageName());
            if (project.isMiniumMode()) {
                setDescriptor = new EndPointSetIntegrateDescriptor(set.getName(),project.getPackageName());
                context.setMiniumMode(true);
            }

            for (HttpEndpoint endpoint: set.getEndpoints()) {

                try {

                    String clazzName = endpoint.getName();
                    clazzName = clazzName.substring(0,1).toUpperCase() + clazzName.substring(1);
                    String responseName = clazzName + "Data";

                    if (endpoint.getResponseBodyView() == ResponseBodyBlankView.class) {
                        responseName = "Void";
                    }
                    buildResponseClass(project,context,responseName,set,endpoint);
                    DataDescriptor requestor = buildRequestClass(project,context,clazzName,set,endpoint,responseName);

                    setDescriptor.addRequest(endpoint.getName(),clazzName);
                    if (setDescriptor instanceof EndPointSetIntegrateDescriptor) {
                        EndPointSetIntegrateDescriptor miniumDesc = (EndPointSetIntegrateDescriptor)setDescriptor;
                        miniumDesc.addRequestor(requestor);
                    }

                } catch (Exception e) {

                    logger.error("Failed to generate source : ", e);
                    emitter.emit(new StatusEvent(
                            String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),endpoint.getName()),
                            0d,true
                    ));

                }

            }

            for (DataDescriptor requests : context.getRequests()) {

                File target = new File(setPackage.getAbsolutePath(), requests.getClassName() + ".java");

                emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.WRITE_FILE) + target.getName()));

                try(FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(requests
                            .buildRequestClass(template)
                            .getBytes(StandardCharsets.UTF_8)
                    );
                    sourceClasses.add(target);
                } catch (Exception e) {
                    logger.error("Failed to write file : ", e);
                    emitter.emit(new StatusEvent(
                            String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),target.getName()),
                            0d,true
                    ));
                    return;
                }

            }

            for (DataDescriptor resps : context.getResponses()) {

                File target = new File(setPackage.getAbsolutePath(), resps.getClassName() + ".java");
                emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.WRITE_FILE) + target.getName()));

                try(FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(resps.buildResponseClass(template)
                            .getBytes(StandardCharsets.UTF_8)
                    );
                    sourceClasses.add(target);
                } catch (Exception e) {
                    logger.error("Failed to write file : ", e);
                    emitter.emit(new StatusEvent(
                            String.format(bundle.getString(LanguageKeys.DLG_CANNOT_WRITE_FILE),target.getName()),
                            0d,true
                    ));
                    return;
                }

            }

            String descSetClass = setDescriptor.generateSetClass(template);
            File descSetClassFile = new File(setPackage.getAbsolutePath(),setDescriptor.getClassName() + ".java");
            try (FileOutputStream fos = new FileOutputStream(descSetClassFile)){
                fos.write(descSetClass.getBytes(StandardCharsets.UTF_8));
                clientDescriptor.addClientApi(set.getName(), setDescriptor.getClassName());
                sourceClasses.add(descSetClassFile);
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
            sourceClasses.add(projectClassFile);
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
                sdkPackager.doPackage(sourceClasses);
            } catch (Exception e) {
                logger.error("Failed to write file : ", e);
                emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.DLG_COMPILE_FAILED),0,true));
                return;
            }
        }

        emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.READY)));
        emitter.emit(new StatusEvent(bundle.getString(LanguageKeys.GENERATED),0,true));
    }

    private DataDescriptor buildRequestClass(SDKProject project,DataDescriptorContext context, String className, HttpEndpoints set, HttpEndpoint endpoint, String responseName) {

        List<DataDescriptor> resultList = new ArrayList<>();
        if (endpoint.getRequestBodyView() == RequestBlankView.class) {

            // blank
            BlankDescriptorGenerator generator = new BlankDescriptorGenerator(project.getPackageName());
            resultList = generator.generateClasses(className,responseName,set,endpoint,null);

        } else if (endpoint.getRequestBodyView() == RequestJsonBodyView.class) {
            // json
            ObjectMapper mapper = new ObjectMapper();
            try {
                String exampleJsonSource = endpoint.getRequestBodyRaw().get(RequestJsonBodyView.class);
                if (exampleJsonSource == null) {
                    exampleJsonSource = "";
                }
                JsonNode node = mapper.readTree(exampleJsonSource);
                JsonDescriptorGenerator generator = new JsonDescriptorGenerator(project.getPackageName());
                resultList = generator.generateClasses(className,responseName,set,endpoint,node);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (endpoint.getRequestBodyView() == RequestUrlEncodedBodyView.class){

            UrlEncodedDescriptorGenerator generator = new UrlEncodedDescriptorGenerator(project.getPackageName());
            String source = endpoint.getRequestBodyRaw().get(endpoint.getRequestBodyView());
            resultList = generator.generateClasses(className,responseName,set,endpoint,source);

        }

        DataDescriptor result = null;
        if (resultList != null) {
            for (DataDescriptor descriptor: resultList) {
                if (descriptor.isSender()) {
                    context.addRequest(descriptor);
                } else {
                    context.addRequestData(descriptor);
                }
                if (descriptor.isSender()) {
                    result = descriptor;
                }
            }
        }

        return result;

    }


    private void buildResponseClass(SDKProject project,DataDescriptorContext context, String className, HttpEndpoints set, HttpEndpoint endpoint) {

        List<DataDescriptor> descriptors = new ArrayList<>();

        if (endpoint.getResponseBodyView() == ResponseBodyJsonView.class) {

            // json
            ObjectMapper mapper = new ObjectMapper();
            try {

                JsonNode node = mapper.readTree(
                        endpoint.getResponseBodyRaw().get(ResponseBodyJsonView.class)
                );

                JsonDescriptorGenerator generator = new JsonDescriptorGenerator(project.getPackageName());
                descriptors = generator.generateClasses(className,null,set,endpoint,node);

                for (DataDescriptor descriptor: descriptors) {
                    context.addResponse(descriptor);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }


}
