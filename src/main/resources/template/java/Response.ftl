<#if packageName??>
package ${packageName};
</#if>

<#list imports as importItem>
import ${importItem};
</#list>

public class ${className} <#if superClass??>extends ${superClass}</#if> {

    <#list fields as field>
    @JsonProperty("${field.propertyName}")
    private ${field.simpleTypeName} ${field.name};

    </#list>

    <#list fields as field>
    public ${field.simpleTypeName} ${field.name}() {
        return this.${field.name};
    }

    </#list>

}