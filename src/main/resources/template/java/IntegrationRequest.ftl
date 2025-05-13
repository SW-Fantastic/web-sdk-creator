    public static class ${className} <#if superClass??>extends ${superClass}</#if> {

        <#if sender>
        private Client client;

        ${className}(Client client) {
            this.client = client;
        }
        <#else>
        public ${className}() {
        }
        </#if>

        <#list fieldClassMap.keySet() as fieldKey>
        @JsonProperty("${fieldKey}")
        private ${fieldClassMap[fieldKey]} ${fieldNameMap[fieldKey]};

        </#list>

        <#if sender>
        <#list fieldParamMap.keySet() as fieldKey>
        @JsonIgnore
        @WebParam("${fieldKey}")
        private ${fieldParamMap[fieldKey]} ${fieldNameMap[fieldKey]};

        </#list>

        <#list webHeaders.keySet() as fieldKey>
        @JsonIgnore
        @RequestHeader("${fieldKey}")
        private String ${fieldNameMap[fieldKey]} = "${webHeaders[fieldKey]}";

        </#list>
        </#if>

        <#list fieldClassMap.keySet() as fieldKey>
        public ${className} ${fieldNameMap[fieldKey]}(${fieldClassMap[fieldKey]} ${fieldNameMap[fieldKey]}) {
            this.${fieldNameMap[fieldKey]} = ${fieldNameMap[fieldKey]};
            return this;
        }

        </#list>

        <#if sender>
        <#list fieldParamMap.keySet() as fieldKey>
        public ${className} ${fieldNameMap[fieldKey]}(${fieldParamMap[fieldKey]} ${fieldNameMap[fieldKey]}) {
            this.${fieldNameMap[fieldKey]} = ${fieldNameMap[fieldKey]};
            return this;
        }

        </#list>

        <#list webHeaders.keySet() as fieldKey>
        public ${className} ${fieldNameMap[fieldKey]}(String ${fieldNameMap[fieldKey]}) {
            this.${fieldNameMap[fieldKey]} = ${fieldNameMap[fieldKey]};
            return this;
        }

        </#list>

        public ${targetClassName} send() {
            return client.send("${targetUrl}", this, <#if contentType??>"${contentType}"<#else>null</#if>, "${method}", ${targetClassName}.class);
        }

        public <T> T sendAndCast(Class<T> targetResponseType) {
            return client.send("${targetUrl}", this, <#if contentType??>"${contentType}"<#else>null</#if>, "${method}", targetResponseType);
        }
        </#if>

    }