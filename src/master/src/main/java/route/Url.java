package route;

import io.netty.handler.codec.http.HttpMethod;
import types.TemplateRoute;

public class Url extends TemplateRoute {
    /**
     * Constructor
     *
     * @param method http
     * @param path path
     */
    public Url(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Get template path
     * @return path to the template
     */
    @Override
    public String GetTemplate() {
        return "/url.twig";
    }
}