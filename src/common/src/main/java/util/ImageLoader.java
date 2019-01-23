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
     * @return scaled image
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
     * Resize with keep aspect ratio
     * https://stackoverflow.com/questions/10245220/java-image-resize-maintain-aspect-ratio
     * @param image buffer
     * @param scaledWidth bew width
     * @param scaledHeight new height
     * @param preserveRatio keep aspect ratio
     * @return scaled image
     */
    public static BufferedImage Resize(BufferedImage image, int scaledWidth, int scaledHeight, boolean preserveRatio) {
        // Keep aspect ratio
        if (preserveRatio) {
            double imageHeight = image.getHeight();
            double imageWidth = image.getWidth();

            if (imageHeight/scaledHeight > imageWidth/scaledWidth) {
                scaledWidth = (int) (scaledHeight * imageWidth / imageHeight);
            } else {
                scaledHeight = (int) (scaledWidth * imageHeight / imageWidth);
            }
        }

        // creates output image
        BufferedImage outputBufImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputBufImage.createGraphics();
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputBufImage;
    }

    /**
     * Transpose
     * @param image to transpose
     * @return transposed image
     */
    public static BufferedImage Transpose(BufferedImage image) {
        // https://en.wikipedia.org/wiki/In-place_matrix_transposition#Square_matrices
        //for n = 0 to N - 2
        //    for m = n + 1 to N - 1
        //        swap A(n,m) with A(m,n)
        assert image.getWidth() == image.getHeight();
        for (int n = 0; n <= image.getWidth() - 2; n++) {
            for (int m = n + 1; m <= image.getHeight() - 1; m++) {
                int old = image.getRGB(n, m);
                image.setRGB(n, m, image.getRGB(m, n));
                image.setRGB(m, n, old);
            }
        }

        return image;
    }

    /**
     * Process image
     * @param i BufferedImage
     * @param width expected width
     * @param height expected height
     * @param keepAspectRatio keep aspect ratio
     * @param transpose transpose image
     * @return image buffer
     */
    public BufferedImage ProcessImage(BufferedImage i, int width, int height,
                                      boolean keepAspectRatio, boolean transpose) {
        // Copy
        BufferedImage temp = i;

        // Resize image
        if (i.getHeight() != height || i.getWidth() != width) {
            temp = Resize(i, width, height, keepAspectRatio);
        }

        // Transpose image
        if (transpose) {
            temp = Transpose(temp);
        }

        return temp;
    }

    /**
     * Process image
     * @param i BufferedImage
     * @param width expected width
     * @param height expected height
     * @param type image type png, jpg etc.
     * @param keepAspectRatio keep aspect ratio
     * @param transpose transpose image
     * @return transport array
     */
    public byte[] ProcessImage(BufferedImage i, int width, int height,
                               String type, boolean keepAspectRatio, boolean transpose) {
        // Copy
        BufferedImage temp = i;
        byte[] ret = null;

        // Resize image
        if (i.getHeight() != height || i.getWidth() != width) {
            temp = Resize(i, width, height, keepAspectRatio);
        }

        // Transpose image
        if (transpose) {
            temp = Transpose(temp);
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
