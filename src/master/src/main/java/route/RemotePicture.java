package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;

import message.ImageMessage;
import types.Client;
import types.Master;
import types.MessageRoute;
import types.WebSession;

import util.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public class RemotePicture extends MessageRoute {
    /**
     * Clients
     */
    private List<Client> clients_ = null;

    /**
     * We support post
     * @return true
     */
    @Override
    public boolean SupportsPost() {
        return true;
    }

    /**
     * Constructor
     *
     * @param method
     * @param path
     */
    public RemotePicture(HttpMethod method, String path) {
        super(method, path);
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
        boolean processLocal = false;

        // Image
        ImageMessage m = new ImageMessage();

        // No clients are connected
        if (clients_.size() == 0) {
            msg.message_ = "Error: There are no nodes registered currently, please try again later.";
            msg.type_ = "danger";
            //return msg;
        }

        // Message
        boolean transpose = false;
        String url = "";
        float brightness = 0.3f;    // Default
        long duration = 0;
        for(Attribute a : session.GetAttributes()) {
            switch(a.getName()) {
                case "processLocal":
                    processLocal = true;
                    break;

                case "transpose":
                    m.transpose_ = true;
                    break;

                case "url":
                    try {
                        url = a.getValue();
                    } catch (IOException e) {}
                    break;

                case "brightness":
                    try {
                        m.brightness_ = Float.parseFloat(a.getValue());
                    } catch (IOException e) {}
                    break;

                case "duration":
                    try {
                        m.duration_ = Integer.parseInt(a.getValue());
                    } catch (IOException e) {}
                    break;
            }
        }

        // Get url
        URL website = null;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            msg.message_ = "Error: Invalid url, please use a valid url.";
            msg.type_ = "danger";
            return msg;
        }

        // Try to create a channel
        ReadableByteChannel rbc = null;
        try {
            rbc = Channels.newChannel(website.openStream());
        } catch (IOException e) {
            msg.message_ = "Error: Invalid source, error message: " + e.getMessage();
            msg.type_ = "danger";
            return msg;
        }

        // Read file into buffer
        ByteBuffer bb = ByteBuffer.allocate(32000);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        WritableByteChannel osc = Channels.newChannel(data);
        try {
            while (rbc.read(bb) != -1) {
                bb.flip();
                osc.write(bb);
                bb.clear();
            }
        } catch (IOException e) {
            msg.message_ = "Error: Failed to download file, error message: " + e.getMessage();
            msg.type_ = "danger";
            return msg;
        }

        // Try to load it
        ImageLoader loader = new ImageLoader();
        BufferedImage i = loader.FromBuffer(data.toByteArray());
        if (i == null) {
            msg.message_ = "Error: Given image is invalid!";
            msg.type_ = "danger";
            return msg;
        }

        // Get mime type from byte array
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.toByteArray()));
        try {
            msg.type_ = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            msg.type_ = "image/jpg";
        }

        // Loop trough clients
        for (Client client : clients_) {
            if (processLocal) {
                m.image_ = loader.ProcessImage(i, client.GetWidth(), client.GetHeight(), m.type_);

                // Done, just display it
                m.processed_ = true;
            } else {
                m.image_ = data.toByteArray();

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