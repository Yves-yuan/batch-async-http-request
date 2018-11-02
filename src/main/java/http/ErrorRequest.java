package http;

public class ErrorRequest {
    private String url;
    private Exception e;

    ErrorRequest(String u, Exception ei) {
        url = u;
        e = ei;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }
}
