package route;

import io.netty.handler.codec.http.HttpMethod;
import org.jtwig.JtwigModel;
import types.TemplateRoute;

public class Index extends TemplateRoute {
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
     * Get template path
     * @return path to the template
     */
    @Override
    public String GetTemplate() {
        return "/index.twig";
    }

    /**
     * Assign
     * @param model JtwigModel
     * @return model
     */
    @Override
    protected JtwigModel Assign(JtwigModel model) {
        return super.Assign(model);
    }
}
