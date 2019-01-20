package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageLoader {
    /**
     * Read image from file
     * @param path to the file on the disk
     * @return Image if sucess
     */
    public BufferedImage FromFile(File path) {
        try {
            return ImageIO.read(path);
        } catch (IOException e) {
            // Happens I guess!
        }
        return null;
    }

    /**
     * Get from buffer
     * @param buffer byte array
     * @return image or null
     */
    public BufferedImage FromBuffer(byte[] buffer) {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        return FromStream(stream);
    }

    /**
     * Get image from stream
     * @param stream input stream
     * @return image or null
     */
    public BufferedImage FromStream(InputStream stream) {
        try {
            return ImageIO.read(stream);
        } catch (IOException e) { }
        return null;
    }

    /**
     * Resize a buffered image
     * https://stackoverflow.com/questions/9417356/bufferedimage-resize
     * @param img buffer
     * @param newW new width
     * @param newH new height
     * @return
     */
    public BufferedImage Resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    /**
     * Process image
     * @param i BufferedImage
     * @param width expected width
     * @param height expected height
     * @param type image type png, jpg etc.
     * @return transport array
     */
    public byte[] ProcessImage(BufferedImage i, int width, int height, String type) {
        // Copy
        BufferedImage temp = i;
        byte[] ret = null;

        // Resize client
        if (i.getHeight() != height || i.getWidth() != width) {
            temp = Resize(i, height, width);
        }

        // TODO: Move to utilities
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(temp, type, stream );
            stream.flush();
            ret = stream.toByteArray();
            stream.close();
        } catch (IOException ignored) {}

        return ret;
    }
}
