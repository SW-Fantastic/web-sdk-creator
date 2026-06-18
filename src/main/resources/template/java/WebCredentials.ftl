package ${basePackageName};

import org.apache.http.HttpRequest;

public interface WebCredentials {

    boolean initialize(HttpRequest request);

}