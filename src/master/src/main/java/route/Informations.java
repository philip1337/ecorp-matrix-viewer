package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import types.TemplateRoute;
import types.WebSession;

public class Informations extends TemplateRoute {
    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public Informations(HttpMethod method, String path) {
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
