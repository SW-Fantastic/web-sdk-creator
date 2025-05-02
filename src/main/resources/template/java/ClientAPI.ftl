package ${basePackageName};

import ${basePackageName}.Client;
import ${basePackageName}.WebCredentials;

<#list clientNamedMaps.keySet() as key>
import ${basePackageName}.${key}.${clientNamedMaps[key]};
</#list>

public class ${className} {

   private Client baseApiClient;

   private WebCredentials cred;

   <#list clientNamedMaps.keySet() as key>
   private ${clientNamedMaps[key]} ${key} = null;

   </#list>

    public ${className}(WebCredentials credential, String baseUrl) {
        if(credential == null) {
            throw new RuntimeException("Credentials can not be null");
        }
        this.cred = credential;
        this.baseApiClient = new Client(this.cred, baseUrl);
    }

    public ${className} unsafe() {

        try {
            this.baseApiClient.unsafe();
            return this;
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    <#list clientNamedMaps.keySet() as key>
    public ${clientNamedMaps[key]} ${key}() {
        if(this.${key} == null) {
            this.${key} = new ${clientNamedMaps[key]}(this.baseApiClient);
        }
        return this.${key};
    }

    </#list>


}