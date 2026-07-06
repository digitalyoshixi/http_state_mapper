package webmapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import de.learnlib.exception.SULException;
import de.learnlib.sul.SUL;

public class WebSUL implements SUL<String, String> {
    private String baseUrl;
    private HttpClient client;
    private CookieManager cookieManager;

    public WebSUL(String baseUrl) {
        this.baseUrl = baseUrl;
        this.cookieManager = new CookieManager();
        this.client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();
    }
    
    @Override
    public void pre() {
        // Clear local cookies AND force server-side session teardown.
        cookieManager.getCookieStore().removeAll();
        try {
            send("/", "GET", ""); // send request to base url
        } catch (IOException | InterruptedException e) {
            throw new SULException(e);
        }
    }

    @Override
    public void post() {
        // Nothing needed if pre() fully resets; keep client alive for reuse.
    }

    @Override
    public String step(String input) throws SULException {
        return "Not Implemented...";
        // convert input into HTTP request
        //I convertedInput = ...
        //String HTTPMessage = send(convertedInput);
        //return WebInputMapper.abstract_output(input);
    }

    private HttpResponse<String> send(String path, String method, String form) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher body = (form == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(form);
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .method(method, body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString());
    }

}
