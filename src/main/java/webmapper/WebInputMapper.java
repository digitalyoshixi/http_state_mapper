package webmapper;

import java.util.Base64;

public class WebInputMapper {

    private Base64.Encoder encoder = Base64.getEncoder();

    public WebInputMapper() {
        super();
    }
    String abstract_input(HTTPMessage request) {
        //return request.getUrl() + " " + request.getMethod() + encoder.encodeToString(request.getBody().getBytes());
        return request.getOrigin() + request.getUrl() + " " + request.getMethod();
    }
    String abstract_output(HTTPMessage request) {
        //return request.getStatus() + encoder.encode(request.getResponse().getBytes());
        return request.getOrigin() + request.getStatus();
    }
}