package webmapper;

public class WebInputClassifier {
   public boolean classify(HTTPMessage request) {
        return "200".equals(request.getStatus());
   }
}
