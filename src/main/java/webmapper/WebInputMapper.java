package webmapper;

import java.util.Base64;

public static class WebInputMapper {

    private Base64.Encoder encoder = Base64.getEncoder();

    public WebInputMapper() {
        super();
    }
    public static String abstract_input(HTTPMessage request) {
        //return request.getUrl() + " " + request.getMethod() + encoder.encodeToString(request.getBody().getBytes());
        return request.getMethod() + " " + request.getUrl();
    }
    public static String abstract_output(HTTPMessage request) {
        //return request.getStatus() + encoder.encode(request.getResponse().getBytes());
        return request.getOrigin() + request.getStatus();
    }
}