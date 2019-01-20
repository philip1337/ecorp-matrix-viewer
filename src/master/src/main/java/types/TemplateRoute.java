package types;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class TemplateRoute extends WebRoute {
    /**
     * Template engine
     */
    protected JtwigTemplate template_ = null;

    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public TemplateRoute(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Content type
     * @return string
     */
    @Override
    public String GetType() {
        return "Content-Type: text/html; charset=utf-8";
    }

    /**
     * Get template
     * @return string
     */
    public String GetTemplate() {
        return "";
    }

    /**
     * On route initialize
     * @return true if success
     */
    @Override
    protected boolean OnInit() {
        // Load template
        byte[] data = null;
        try {
            data = provider_.Get(GetTemplate());
        } catch (IOException e) {
            return false;
        }

        // Invalid
        if (data == null)
            return false;

        // Get template
        try {
            template_ = JtwigTemplate.inlineTemplate(new String(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO: Log
            return false;
        }
        return true;
    }

    /**
     * Assign
     * @param model JtwigModel
     * @return model
     */
    protected JtwigModel Assign(JtwigModel model) {
        return model.with("corp", Config.CORP_NAME)
                    .with("year", Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * Process
     * @param buffer data
     * @param request http request
     * @param session (current request)
     */
    @Override
    public void Process(StringBuilder buffer, HttpRequest request, WebSession session) {
        // Process
        super.Process(buffer, request, session);

        // Get output stream
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        // Process template
        ProcessTemplate(o, request, session);

        // Append rendered template
        buffer.append(o.toString());
    }

    /**
     *
     * @param output stream
     * @param request http request
     * @param session (current request)
     */
    public void ProcessTemplate(OutputStream output, HttpRequest request, WebSession session) {
        template_.render(Assign(JtwigModel.newModel()), output);
    }
}
