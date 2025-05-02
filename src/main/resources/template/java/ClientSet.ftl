package ${basePackageName}.${name};

import ${basePackageName}.Client;

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

}