package ${basePackageName}.${name};

<#list importList as importItem>
import ${importItem};
</#list>

import ${basePackageName}.Client;
import ${basePackageName}.WebParam;
import ${basePackageName}.RequestHeader;

public class ${className} {

    private Client client;

    public ${className}(Client client) {
        this.client = client;
    }

    <#list requestNamesMap.keySet() as key>
    public ${requestNamesMap[key]} ${key}() {
        return new ${requestNamesMap[key]}(client);
    }

    </#list>

<#list requestors as requestor>
${requestor}
</#list>

}