package org.swdc.websdk.core.generator.java;

import freemarker.cache.ByteArrayTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import jakarta.inject.Singleton;
import org.swdc.websdk.SDKApplication;

import java.io.StringWriter;

@Singleton
public class JavaSDKTemplate {

    private Configuration configuration;


    public JavaSDKTemplate() {

        ByteArrayTemplateLoader templateLoader = new ByteArrayTemplateLoader();

        loadTemplate(templateLoader,"Request.ftl");
        loadTemplate(templateLoader,"Client.ftl");
        loadTemplate(templateLoader,"WebCredentials.ftl");
        loadTemplate(templateLoader,"WebParam.ftl");
        loadTemplate(templateLoader,"Pom.ftl");
        loadTemplate(templateLoader,"ClientSet.ftl");
        loadTemplate(templateLoader,"ClientAPI.ftl");
        loadTemplate(templateLoader,"Response.ftl");
        loadTemplate(templateLoader,"IntegrationClientSet.ftl");
        loadTemplate(templateLoader,"IntegrationRequest.ftl");

        configuration = new Configuration(Configuration.VERSION_2_3_21);
        configuration.setObjectWrapper(new BeansWrapper(Configuration.VERSION_2_3_21));
        configuration.setTemplateLoader(templateLoader);

    }

    private void loadTemplate(ByteArrayTemplateLoader templateLoader, String name) {
        try {
            Module module = SDKApplication.class.getModule();
            byte[] data = module.getResourceAsStream("template/java/" + name).readAllBytes();
            templateLoader.putTemplate(name,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String render(String template, Object data) {
        try {
            StringWriter writer = new StringWriter();
            configuration.getTemplate(template).process(data,writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
