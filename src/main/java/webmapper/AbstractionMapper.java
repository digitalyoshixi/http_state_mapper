package webmapper;

public abstract class AbstractionMapper {
    public AbstractionMapper() {
    }
    abstract String abstract_input(XMLRequest request);
    abstract String abstract_output(XMLRequest request);
}
