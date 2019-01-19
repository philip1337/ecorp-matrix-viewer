package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import types.WebRoute;
import types.WebSession;

public class NotFound extends WebRoute {
    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public NotFound(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * 404 Route
     * @param buffer data
     * @param request http request
     * @param session (current request)
     */
    @Override
    public void Process(StringBuilder buffer, HttpRequest request, WebSession session) {

    }
}