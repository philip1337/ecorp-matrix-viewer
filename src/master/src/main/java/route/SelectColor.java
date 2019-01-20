package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import types.TemplateRoute;
import types.WebSession;

public class SelectColor extends TemplateRoute {
    /**
     * Constructor
     *
     * @param method http
     * @param path path
     */
    public SelectColor(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Get template path
     * @return path to the template
     */
    @Override
    public String GetTemplate() {
        return "/color.twig";
    }
}