package ${basePackageName};

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static Pattern BLANK_STR_MATCHER = Pattern.compile("^[\\s]+$");

    private static String UTF_8 = "UTF-8";

    private WebCredentials cred;

    private String baseUrl;

    private HttpClient client = null;

    private ObjectMapper mapper;

    private volatile int maxRetry = 1;

    private List<Class> retryExceptions = new ArrayList<>();

    public Client(WebCredentials credentials, String baseUrl) {

        this.cred = credentials;
        this.baseUrl = baseUrl;
        this.mapper = new ObjectMapper();
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.client = HttpClientBuilder.create()
                        .setRetryHandler(this::retryHandler)
                        .build();

        if(this.baseUrl.endsWith("/")) {
            this.baseUrl = this.baseUrl.substring(0,this.baseUrl.length() - 1);
        }

    }

    private boolean retryHandler(IOException e, int execCount, HttpContext httpContext) {
        if (execCount >= maxRetry) {
            return false;
        }
        Throwable ex = e;
        while (ex != null) {
            if (retryExceptions.contains(ex.getCause())) {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    public Client maxRetry(int times) {
        this.maxRetry = times;
        return this;
    }

    public Client retryWith(Class<? extends Exception> exception) {
        if (this.retryExceptions.contains(exception)) {
            return this;
        }
        retryExceptions.add(exception);
        return this;
    }

    public void unsafe() {

        X509TrustManager unsafeManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new TrustManager[]{ unsafeManager }, new SecureRandom());

            Registry<ConnectionSocketFactory> factoryRegistry = RegistryBuilder.
                    <ConnectionSocketFactory>create()
                    .register("http",PlainConnectionSocketFactory.INSTANCE)
                    .register("https",new SSLConnectionSocketFactory(context))
                    .build();

            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(
                    factoryRegistry
            );

            this.client = HttpClientBuilder.create()
                    .setConnectionManager(connMgr)
                    .setRetryHandler(this::retryHandler)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public <T> T send(String url, Object param, String contentType, String method, Class<T> responseType) {

        Field[] fields = param.getClass().getDeclaredFields();
        Map<String,Object> params = new HashMap<>();
        for (Field field: fields) {

            WebParam property = field.getAnnotation(WebParam.class);
            if (property == null) {
                continue;
            }
            try {
                field.setAccessible(true);
                params.put(property.value(),field.get(param));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        String requestUrl = buildRequestUrl(params,url);
        HttpUriRequest request = createRequest(method.toUpperCase(),requestUrl);

        if (request instanceof HttpEntityEnclosingRequestBase) {
            HttpEntityEnclosingRequestBase entityRequest = (HttpEntityEnclosingRequestBase) request;
            HttpEntity entity = null;
            if (contentType.equals("form-url-encoded")) {
                request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                try {
                    entity = new UrlEncodedFormEntity(createEncoded(param));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (contentType.equals("json")) {
                request.addHeader("Content-Type", "application/json");
                try {
                    entity = new StringEntity(mapper.writeValueAsString(param));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            entityRequest.setEntity(entity);
        }

        try {
            cred.initialize(request);
            HttpResponse response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            if (statusLine.getStatusCode() != 200) {
                if (statusLine.getStatusCode() == 404) {
                    return null;
                }
                String content = "";
                if (entity.getContent() != null) {
                    content = new String(readAllBytes(entity.getContent()), UTF_8);
                }
                throw new RuntimeException(statusLine.getStatusCode() + " " + content);
            }

            if (entity == null) {
                return null;
            }
            if (responseType == Void.class) {
                return null;
            }
            return mapper.readValue(
                    entity.getContent(),responseType
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String buildRequestUrl(Map<String,Object> params, String url) {

        if (url.endsWith("/")) {
            url = url.substring(0,url.length() - 1);
        }

        String[] parts = url.split("/");
        StringBuilder targetUrl = new StringBuilder();

        for (int idx = 0; idx < parts.length;idx ++) {
            String part = parts[idx];
            if(isStringBlank(part)) {
                continue;
            }
            if (part.startsWith("{") && part.endsWith("}")) {
                part = part.replace("{", "").replace("}", "");
                Object data = params.remove(part);
                if (data == null) {
                    throw new RuntimeException("Parameter " + part + " is required.");
                }
                part = data.toString();
            }
            targetUrl.append("/").append(part);
        }

        boolean qsAppend = false;
        for (Map.Entry<String,Object> ent : params.entrySet()) {

            if (ent.getValue() == null) {
                continue;
            }

            if (!qsAppend) {
                targetUrl.append("?");
                qsAppend = true;
            } else {
                targetUrl.append("&");
            }

            if (ent.getValue().getClass().isArray()) {

                Object[] array = (Object[]) ent.getValue();
                for (int idx = 0; idx < array.length; idx ++) {

                    try {

                        String val = URLEncoder.encode(
                                array[idx].toString(),UTF_8
                        );
                        targetUrl.append(ent.getKey()).append("=")
                                .append(val);
                        if (idx + 1 < array.length - 1) {
                            targetUrl.append("&");
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            } else {
                try {
                    String val = URLEncoder.encode(
                            ent.getValue().toString(),UTF_8
                    );
                    targetUrl.append(ent.getKey()).append("=")
                            .append(val);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String requestUrl = targetUrl.toString();
        if(!requestUrl.startsWith("/")) {
            requestUrl = "/" + requestUrl;
        }

        return baseUrl + requestUrl;
    }

    private List<NameValuePair> createEncoded(Object data) {

        Field[] fields = data.getClass().getDeclaredFields();
        try {
            List<NameValuePair> pairs = new ArrayList<>();
            for (Field field: fields) {
                JsonProperty property = field.getAnnotation(JsonProperty.class);
                if (property == null) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(data);
                if(value == null) {
                    continue;
                }
                BasicNameValuePair pair = new BasicNameValuePair(property.value(), value.toString());
                pairs.add(pair);
            }
            return pairs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private HttpUriRequest createRequest(String method, String url) {
        switch (method) {
            case "GET":
                return new HttpGet(url);
            case "PUT":
                return new HttpPut(url);
            case "POST":
                return new HttpPost(url);
            case "DELETE":
                return new HttpDelete(url);
            case "PATCH":
                return new HttpPatch(url);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private boolean isStringBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        Matcher matcher = BLANK_STR_MATCHER.matcher(str);
        return matcher.matches();
    }

    private byte[] readAllBytes(InputStream is) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024 * 1024];
            int bytes = 0;
            while ((bytes = is.read(buf)) != -1) {
                bos.write(buf,0,bytes);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}