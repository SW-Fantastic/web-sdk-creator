<#if packageName??>
package ${packageName};
</#if>

<#list importList as importItem>
import ${importItem};
</#list>

public class ${className} <#if superClass??>extends ${superClass}</#if> {

    <#list fieldClassMap.keySet() as fieldKey>
    @JsonProperty("${fieldKey}")
    private ${fieldClassMap.get(fieldKey)} ${fieldNameMap.get(fieldKey)};

    </#list>

    <#list fieldClassMap.keySet() as fieldKey>
    public ${fieldClassMap.get(fieldKey)} ${fieldNameMap.get(fieldKey)}() {
        return this.${fieldNameMap.get(fieldKey)};
    }

    </#list>

}