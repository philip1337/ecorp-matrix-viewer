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
     * Initialize route
     */
    public Provider provider_ = null;

    /**
     * Constructor
     * @param method http type
     * @param path request uri
     */
    public WebRoute(final HttpMethod method, final String path) {
        this.method_ = method;
        this.path_ = path;
    }

    /**
     * Default type
     * @return content type
     */
    public String GetType() {
        return "text/plain; charset=UTF-8";
    }

    /**
     * Initialize
     * @param provider (veloxio data provider)
     * @return true if route is ready to run
     */
    boolean Initialize(Provider provider) {
        provider_ = provider;
        return OnInit();
    }

    protected boolean OnInit() {return true;}

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
    boolean Matches(final HttpMethod method, final String path) {
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