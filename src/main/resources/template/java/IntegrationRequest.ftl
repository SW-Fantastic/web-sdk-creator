    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ${requestClassName} <#if superClass??>extends ${superClass}</#if> {

        <#if requestor>
        private Client client;

        ${requestClassName}(Client client) {
            this.client = client;
        }
        <#else>
        public ${requestClassName}() {
        }
        </#if>

        <#list requestFields as field>
        @JsonProperty("${field.propertyName}")
        private ${field.simpleTypeName} ${field.name};

        </#list>

        <#if requestor>
        <#list allParams as field>
        @JsonIgnore
        @WebParam("${field.propertyName}")
        private ${field.simpleTypeName} ${field.name};

        </#list>

        <#list headers as field>
        @JsonIgnore
        @RequestHeader("${field.propertyName}")
        private String ${field.name} = "${field.defaultValue}";

        </#list>
        </#if>

        <#list requestFields as field>
        public ${requestClassName} ${field.name}(${field.simpleTypeName} value) {
            this.${field.name} = value;
            return this;
        }

        </#list>

        <#if requestor>
        <#list allParams as field>
        public ${requestClassName} ${field.name}(${field.simpleTypeName} value) {
            this.${field.name} = value;
            return this;
        }

        </#list>

        <#list headers as field>
        public ${requestClassName} ${field.name}(${field.simpleTypeName} value) {
            this.${field.name} = value;
            return this;
        }

        </#list>
        public ${responseClassName} send() {
            return client.send("${targetUrl}", this, <#if contentType??>"${contentType}"<#else>null</#if>, "${httpMethod}", ${responseClassName}.class);
        }

        public <T> T sendAndCast(Class<T> targetResponseType) {
            return client.send("${targetUrl}", this, <#if contentType??>"${contentType}"<#else>null</#if>, "${httpMethod}", targetResponseType);
        }
        </#if>
    }