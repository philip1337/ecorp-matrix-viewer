package types;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import veloxio.Provider;

public class WebRoute {
    /**
     * Request method
     */
    private final HttpMethod method_;

    /**
     * URI
     */
    private final String path_;

    /**
     * Resnponse type
     */
    public String type_ = "text/plain; charset=UTF-8";

    /**
     * Constructor
     * @param method
     * @param path
     */
    public WebRoute(final HttpMethod method, final String path) {
        this.method_ = method;
        this.path_ = path;
    }

    /**
     * Initialize
     * @param provider (veloxio data provider)
     * @return true if route is ready to run
     */
    public boolean Initialize(Provider provider) {
        return true;
    }

    /**
     * Get http method
     * @return method object
     */
    public HttpMethod GetMethod() {
        return method_;
    }

    /**
     * Get patch
     * @return matching uri for this route
     */
    public String GetPath() {
        return path_;
    }

    /**
     * Check if this route is corresponding to the given web uri
     * @param method method (get,post etc.)
     * @param path (request uri)
     * @return true if matches
     */
    public boolean Matches(final HttpMethod method, final String path) {
        return this.method_.equals(method) && this.path_.equals(path);
    }

    /**
     * Request
     * @param buffer data
     * @param request http request
     * @param session (current request)
     */
    public void Process(StringBuilder buffer, HttpRequest request, WebSession session) {

    }

    /**
     * Do we support uploads?
     * @return true if we support it
     */
    public boolean SupportsPost() {
        return false;
    }
}