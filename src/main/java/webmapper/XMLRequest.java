package webmapper;

public class XMLRequest {
    private String url;
    private String method;
    private String body;
    private String headers;
    private String cookies;
    private String response;
    private String status;
    private String time;
    private String date;
    private String server;

    public XMLRequest(String url, String method, String body, String headers, String cookies, String response, String status, String time, String date, String server) {
        this.url = url;
        this.method = method;
        this.body = body;
        this.headers = headers;
        this.cookies = cookies;
        this.response = response;
        this.status = status;
        this.time = time;
        this.date = date;
        this.server = server;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public String getHeaders() {
        return headers;
    }

    public String getCookies() {
        return cookies;
    }

    public String getResponse() {
        return response;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getServer() {
        return server;
    }
}
