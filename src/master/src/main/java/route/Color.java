package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import message.ColorMessage;
import types.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Color extends MessageRoute {
    /**
     * Clients
     */
    private List<Client> clients_ = null;

    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public Color(HttpMethod method, String path) {
        super(method, path);
    }

    /**
     * We support post
     * @return true
     */
    @Override
    public boolean SupportsPost() {
        return true;
    }

    /**
     * Color
     * @param colorStr
     * @return color
     */
    private java.awt.Color Hex2Rgb(String colorStr) {
        return new java.awt.Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 )
        );
    }

    /**
     * Process and return
     * @param output stream
     * @param request http request
     * @param session (current request)
     * @return message
     */
    @Override
    public ReturnMessage ProcessAndReturn(OutputStream output, HttpRequest request, WebSession session) {
        ReturnMessage msg = new ReturnMessage();

        // No clients are connected
        if (clients_.size() == 0) {
            msg.message_ = "Error: There are no nodes registered currently, please try again later.";
            msg.type_ = "danger";
            return msg;
        }

        // Message
        ColorMessage c = new ColorMessage();

        for(Attribute a : session.GetAttributes()) {
            switch(a.getName()) {
                case "color":
                    try {
                        // Extract color from hex
                        java.awt.Color color = Hex2Rgb(a.getValue());

                        // Get color values
                        c.r_ = color.getRed();
                        c.g_ = color.getGreen();
                        c.b_ = color.getBlue();
                    } catch (IOException e) {
                        // Go 4 black if we fail...
                        c.r_ = 0;
                        c.g_ = 0;
                        c.b_ = 0;
                    }
                    break;

                case "brightness":
                    try {
                        c.brightness_ = Float.parseFloat(a.getValue());
                    } catch (IOException e) {
                        c.brightness_ = 1.0f;
                    }
                    break;

                case "duration":
                    try {
                        c.duration_ = Integer.parseInt(a.getValue());
                    } catch (IOException e) {
                        c.duration_ = 0;
                    }
                    break;
            }
        }

        // Clients
        for(Client client : clients_) {
            client.Write(Master.HEADER_MN_COLOR, c);
        }

        // Success
        msg.message_ = String.format("Success, color transferred, showing up: %s",
                c.duration_ == 0 ? "<permanent>" : c.duration_ + " seconds");
        msg.type_ = "success";
        return msg;
    }


    /**
     * Set clients
     * @param nodes client list
     */
    public void SetClients(List<Client> nodes) {
        clients_ = nodes;
    }
}
