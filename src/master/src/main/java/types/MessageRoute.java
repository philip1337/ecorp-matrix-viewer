package types;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import org.jtwig.JtwigModel;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class MessageRoute extends TemplateRoute {
    /**
     * Message
     */
    public class ReturnMessage {
        public String type_;
        public String message_;
    }

    /**
     * Constructor
     *
     * @param method http
     * @param path uri
     */
    public MessageRoute(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * Get template
     * @return template path
     */
    @Override
    public String GetTemplate() {
        return "/message.twig";
    }

    /**
     * Process
     * @param buffer data
     * @param request http request
     * @param session (current request)
     */
    @Override
    public void Process(StringBuilder buffer, HttpRequest request, WebSession session) {
        // Get output stream
        ByteArrayOutputStream o = new ByteArrayOutputStream();

        // Process template
        ProcessTemplate(o, request, session);

        // Append rendered template
        buffer.append(o.toString());
    }

    /**
     * Process and return
     * @param output stream
     * @param request http request
     * @param session (current request)
     * @return display message
     */
    public ReturnMessage ProcessAndReturn(OutputStream output, HttpRequest request, WebSession session) {
        return new ReturnMessage();
    }

    /**
     *
     * @param output stream
     * @param request http request
     * @param session (current request)
     */
    @Override
    public void ProcessTemplate(OutputStream output, HttpRequest request, WebSession session) {
        ReturnMessage msg = ProcessAndReturn(output, request, session);
        template_.render(Assign(JtwigModel.newModel().with("message", msg.message_)
                                                     .with("type", msg.type_)), output);
    }
}
