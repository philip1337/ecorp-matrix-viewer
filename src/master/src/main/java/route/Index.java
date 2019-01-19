package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import types.WebRoute;
import types.WebSession;

public class Index extends WebRoute {
    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public Index(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Index
     * @param buffer data
     * @param request http request
     * @param session (current request)
     */
    @Override
    public void Process(StringBuilder buffer, HttpRequest request, WebSession session) {

    }
}
