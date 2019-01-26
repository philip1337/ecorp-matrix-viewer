package route;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.Attribute;
import message.ImageMessage;
import types.*;
import util.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            return msg;
        }

        // Invalid file upload
        if (session.GetFiles().size() <= 0) {
            msg.message_ = "Error: Failed to read image, please try again later..";
            msg.type_ = "danger";
            return msg;
        }

        // Image message
        ImageMessage m = new ImageMessage();
        boolean processLocal = false;
        boolean storeImage = false;
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

        // Store image
        if (storeImage) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String cachePath = Config.CACHE_FOLDER + df.format(timestamp) + " - " + file.getFilename();

            // Cache path
            File cacheFile = new File(cachePath);
            File directory = cacheFile.getParentFile();

            // Create directories
            if (!directory.exists() && !directory.mkdirs()) {
                msg.message_ = "Error: Cache directory is not writeable.";
                msg.type_ = "danger";
                return msg;
            }

            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(file.get());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // File type
        m.type_ = file.getContentType();

        // Get buffered image
        ImageLoader loader = new ImageLoader();
        List<ImageFrame> frames = new ArrayList<>();

        // Already throw back
        if (!m.type_.contains("image/")) {
            msg.message_ = "Error: File is not an image.";
            msg.type_ = "danger";
            return msg;
        }

        // Differ between gif and normal image
        if (m.type_.equals("image/gif")) {
            try {
                frames = loader.GetFrames(imageBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                msg.message_ = "Error: File is not an image.";
                msg.type_ = "danger";
                return msg;
            }
        } else {
            // Get buffered image
            ImageFrame temp = loader.FromBuffer(imageBuffer);
            if (temp.image_ == null) {
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

        // Answer
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
