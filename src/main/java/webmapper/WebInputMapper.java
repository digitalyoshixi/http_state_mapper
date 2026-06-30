package webmapper;

public class WebInputMapper extends AbstractionMapper {
    public WebInputMapper() {
        super();
    }
    @Override
    String abstract_input(XMLRequest request) {
        return request.getUrl() + " " + request.getMethod();
    }
    @Override
    String abstract_output(XMLRequest request) {
        return request.getStatus();
    }
}