package route;

import io.netty.handler.codec.http.HttpMethod;
import types.TemplateRoute;

public class Upload extends TemplateRoute {
    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public Upload(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Get template path
     * @return path to the template
     */
    @Override
    public String GetTemplate() {
        return "/upload.twig";
    }
}