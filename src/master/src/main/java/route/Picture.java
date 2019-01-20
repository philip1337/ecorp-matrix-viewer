package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.Attribute;
import message.ImageMessage;
import types.*;
import util.ImageLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Picture extends MessageRoute {
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
    public Picture(HttpMethod method, String path) {
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
            //return msg;
        }

        // Invalid file upload
        if (session.GetFiles().size() != 1) {
            msg.message_ = "Error: Failed to read image, please try again later..";
            msg.type_ = "danger";
            return msg;
        }

        // Image message
        ImageMessage m = new ImageMessage();
        boolean processLocal = false;
        for(Attribute a : session.GetAttributes()) {
            switch(a.getName()) {
                case "processLocal":
                    processLocal = true;
                    break;

                case "transpose":
                    m.transpose_ = true;
                    break;

                case "brightness":
                    try {
                        m.brightness_ = Float.parseFloat(a.getValue());
                    } catch (IOException e) {
                        m.brightness_ = 1.0f;
                    }
                    break;

                case "duration":
                    try {
                        m.duration_ = Integer.parseInt(a.getValue());
                    } catch (IOException e) {
                        m.duration_ = 0;
                    }
                    break;
            }
        }

        // Get first
        FileUpload file = session.GetFiles().get(0);
        byte[] imageBuffer = null;
        try {
            imageBuffer = file.get();
            if (imageBuffer.length <= 0) {
                throw new IOException("File too small...");
            }

        } catch (IOException e) {
            msg.message_ = "Error: Invalid file upload with message: " + e.getMessage();
            msg.type_ = "danger";
            return msg;
        }

        // Get buffered image
        BufferedImage i = null;
        ImageLoader loader = new ImageLoader();
        i = loader.FromBuffer(imageBuffer);
        if (i == null) {
            msg.message_ = "Error: File is not an image.";
            msg.type_ = "danger";
            return msg;
        }

        // File type
        m.type_ = file.getContentType();

        // Clients
        for(Client client : clients_) {
            BufferedImage temp = i;
            if (processLocal) {
                m.image_ = loader.ProcessImage(i, client.GetWidth(), client.GetHeight(), m.type_);

                // Done, just display it
                m.processed_ = true;
            } else {
                m.image_ = imageBuffer;

                // Not ready, process on node
                m.processed_ = false;
            }

            // Transfer
            client.Write(Master.HEADER_MN_IMAGE, m);
        }

        msg.message_ = "OK";
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
