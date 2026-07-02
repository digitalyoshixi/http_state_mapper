package webmapper;

public class WebInputMapper {
    public WebInputMapper() {
        super();
    }
    String abstract_input(HTTPMessage request) {
        return request.getUrl() + " " + request.getMethod();
    }
    String abstract_output(HTTPMessage request) {
        return request.getStatus();
    }
}