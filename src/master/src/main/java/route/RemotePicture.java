package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;

import message.ImageMessage;
import types.*;

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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        boolean storeImage = false;

        // Image
        ImageMessage m = new ImageMessage();

        // No clients are connected
        if (clients_.size() == 0) {
            msg.message_ = "Error: There are no nodes registered currently, please try again later.";
            msg.type_ = "danger";
            return msg;
        }

        // Message
        String url = "";
        for(Attribute a : session.GetAttributes()) {
            switch(a.getName()) {
                case "processLocal":
                    processLocal = true;
                    break;

                case "transpose":
                    m.transpose_ = true;
                    break;

                case "storeImage":
                    storeImage = true;
                    break;

                case "aspectRatio":
                    m.keepAspectRatio_ = true;
                    break;

                case "pause":
                    try {
                        m.pause_ = Integer.parseInt(a.getValue());
                    } catch (IOException e) {
                        m.pause_ = 100;
                    }
                    break;

                case "url":
                    try {
                        url = a.getValue();
                    } catch (IOException e) {}
                    break;

                case "brightness":
                    try {
                        int value = Integer.min(100, Integer.parseInt(a.getValue()));
                        m.brightness_ = value / 100;
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

        // Get mime type from byte array
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(data.toByteArray()));
        try {
            m.type_ = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            msg.message_ = "Error: Invalid file format, error message: " + e.getMessage();
            msg.type_ = "danger";
            return msg;
        }

        // We can't get the type so we upload it manually
        if (m.type_ == null) {
            msg.message_ = "Error: Remote (web scrapping) not supported for that image, please upload it manually.";
            msg.type_ = "danger";
            return msg;
        }

        // Store image
        if (storeImage) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String cachePath = Config.CACHE_FOLDER + df.format(timestamp) + website.getFile();

            // Cache path
            File file = new File(cachePath);
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data.toByteArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Get buffered image
        ImageLoader loader = new ImageLoader();
        List<ImageFrame> frames = new ArrayList<>();

        // Differ between gif and normal image
        if (m.type_.equals("image/gif")) {
            try {
                frames = loader.GetFrames(data.toByteArray());
            } catch (IOException e) {
                msg.message_ = "Error: File is not an image.";
                msg.type_ = "danger";
                return msg;
            }
        } else {
            // Get buffered image
            ImageFrame temp = loader.FromBuffer(data.toByteArray());
            if (temp == null) {
                msg.message_ = "Error: File is not an image.";
                msg.type_ = "danger";
                return msg;
            }

            // Add to frame list
            frames.add(temp);
        }

        // Clients
        for(Client client : clients_) {
            // Clean list
            m.image_.clear();

            // Process
            for(ImageFrame i : frames) {
                if (processLocal) {
                    m.image_.add(loader.ProcessImage(i, client.GetWidth(), client.GetHeight(),
                            m.type_.replace("image/", ""), m.keepAspectRatio_, m.transpose_));

                    // Done, just display it
                    m.processed_ = true;
                } else {
                    m.image_.add(loader.ToImageBuffer(i, m.type_.replace("image/", "")));

                    // Not ready, process on node
                    m.processed_ = false;
                }
            }

            // Transfer
            client.Write(Master.HEADER_MN_IMAGE, m);
        }

        msg.message_ = "Image transferred to the display.";
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