package util;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

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
     * Get frames
     * @param gif file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<BufferedImage> GetFrames(File gif) throws IOException{
        ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();
        ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
        ir.setInput(ImageIO.createImageInputStream(gif));
        for(int i = 0; i < ir.getNumImages(true); i++)
            frames.add(ir.getRawImageType(i).createBufferedImage(ir.getWidth(i), ir.getHeight(i)));
        return frames;
    }

    /**
     * Input stream
     * @param buffer file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<BufferedImage> GetFrames(byte[] buffer) throws IOException{
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        return GetFrames(stream);
    }

    /**
     * Input stream
     * @param stream file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<BufferedImage> GetFrames(InputStream stream) throws IOException{
        ArrayList<BufferedImage> frames = new ArrayList<>();
        ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());
        ir.setInput(ImageIO.createImageInputStream(stream));
        for(int i = 0; i < ir.getNumImages(true); i++)
            frames.add(ir.getRawImageType(i).createBufferedImage(ir.getWidth(i), ir.getHeight(i)));
        return frames;
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
     * @return image buffer
     */
    public BufferedImage ProcessImage(BufferedImage i, int width, int height) {
        // Copy
        BufferedImage temp = i;
        byte[] ret = null;

        // Resize client
        if (i.getHeight() != height || i.getWidth() != width) {
            temp = Resize(i, height, width);
        }

        return temp;
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
