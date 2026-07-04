package webmapper;

public class HTTPMessage {
    public String url;
    public String origin;
    public String method;
    public String body;
    public String headers;
    public String cookies;
    public String response;
    public String status;
    public long time;
    public String date;
    public String server;

    public HTTPMessage(String url, String origin, String method, String body, String headers, String cookies, String response, String status, long time, String date, String server) {
        this.url = url;
        this.origin = origin;
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
    
    public String getOrigin() {
        return origin;
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

    public long getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getServer() {
        return server;
    }
}
