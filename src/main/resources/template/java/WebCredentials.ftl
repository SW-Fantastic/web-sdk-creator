package ${basePackageName};

import org.apache.http.HttpRequest;

public interface WebCredentials {

    void initialize(HttpRequest request);

}