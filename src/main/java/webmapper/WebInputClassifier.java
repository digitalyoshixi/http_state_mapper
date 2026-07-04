package webmapper;

public class WebInputClassifier {
   public boolean classify(HTTPMessage request) {
      try {
         int cast_status = Integer.parseInt(request.getStatus());
         return 200 <= cast_status && cast_status <= 400;
      }
      catch (Exception e){ 
         return false;
      }
   }
}
