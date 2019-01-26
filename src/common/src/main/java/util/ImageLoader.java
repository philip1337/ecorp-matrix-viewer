package util;

import org.imgscalr.Scalr;
import org.w3c.dom.Node;
import types.ImageBuffer;
import types.ImageFrame;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
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
    public ImageFrame FromBuffer(byte[] buffer) {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        return FromStream(stream);
    }

    /**
     * From buffer to frame
     * @param b
     * @return
     */
    public ImageFrame FromBufferToFrame(ImageBuffer b) {
        ImageFrame i = FromBuffer(b.image_);
        i.delay_ = b.delay_;
        return i;
    }

    /**
     * Get image from stream
     * @param stream input stream
     * @return image or null
     */
    public ImageFrame FromStream(InputStream stream) {
        try {
            ImageFrame f = new ImageFrame();
            f.image_ = ImageIO.read(stream);
            return f;
        } catch (IOException e) { }
        return null;
    }

    /**
     * Input stream
     * @param buffer file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(byte[] buffer) throws IOException{
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        return GetFrames(stream);
    }

    /**
     * Try to read delay
     * @param root root node metadata
     * @return delay for frame
     */
    private int GetDelay(IIOMetadataNode root) {
        // If root is invalid (Default 10)
        if (root == null)
            return 10;

        // Find GraphicControlExtension node
        int nNodes = root.getLength();
        int d = 0;
        for (int j = 0; j < nNodes; j++) {
            Node node = root.item(j);
            if (node.getNodeName().equalsIgnoreCase("GraphicControlExtension")) {
                // Get delay value
                String delay = ((IIOMetadataNode)node).getAttribute("delayTime");
                d = Integer.parseInt(delay) * 10;
                break;
            }
        }

        return Integer.max(d, 10);
    }

    /**
     * Read
     * @param frames frame array
     * @param reader image reader (buffer)
     * @throws IOException if failed to read from buffer
     */
    private void Read(ArrayList<ImageFrame> frames, ImageReader reader) throws IOException {
        int count = reader.getNumImages(true);
        if (count == 0)
            return;

        // Get 'metaFormatName'. Need first frame for that.
        // https://stackoverflow.com/questions/26801433/fix-frame-rate-of-animated-gif-in-java
        IIOMetadata imageMetaData = reader.getImageMetadata(0);
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        // Loop over frames
        for (int index = 0; index < count; index++) {
            ImageFrame frame = new ImageFrame();
            frame.image_ =  reader.read(index);

            // Try to get delay
            frame.delay_ = GetDelay((IIOMetadataNode)reader.getImageMetadata(index).getAsTree(metaFormatName));
            System.out.printf("delay: %d\n", frame.delay_);
            if (frame.image_ != null)
                frames.add(frame);
        }
    }

    /**
     * Get frames
     * @param gif file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(File gif) throws IOException{
        ArrayList<ImageFrame> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream stream = ImageIO.createImageInputStream(gif);
        reader.setInput(stream);
        Read(frames, reader);
        return frames;
    }

    /**
     * Input stream
     * @param stream file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(InputStream stream) throws IOException{
        ArrayList<ImageFrame> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(stream);
        Read(frames, reader);
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
        // Use scalr image library if we have to keep aspect ratio
        if (preserveRatio) {
            if(image.getHeight() >= image.getWidth()){
                return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, scaledHeight);
            } else{
                return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, scaledWidth);
            }
        }

        // Otherwise we use the standard resize (with scale smooth)

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
    public ImageFrame ProcessImage(ImageFrame i, int width, int height,
                                      boolean keepAspectRatio, boolean transpose) {
        // Copy
        BufferedImage temp = i.image_;

        // Resize image
        if (temp.getHeight() != height || temp.getWidth() != width) {
            temp = Resize(temp, width, height, keepAspectRatio);
        }

        // Transpose image
        if (transpose) {
            temp = Transpose(temp);
        }

        i.image_ = temp;
        return i;
    }

    /**
     * To image buffer
     * @param i image
     * @param type type (gif, jpg, png whatever)
     * @return buffer
     */
    public ImageBuffer ToImageBuffer(ImageFrame i, String type) {
        ImageBuffer ret = new ImageBuffer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(i.image_, type, stream );
            stream.flush();
            ret.image_ = stream.toByteArray();
            stream.close();
        } catch (IOException ignored) {}

        ret.delay_ = i.delay_;
        return ret;
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
    public ImageBuffer ProcessImage(ImageFrame i, int width, int height,
                                    String type, boolean keepAspectRatio, boolean transpose) {
        // Resize image
        if (i.image_.getHeight() != height || i.image_.getWidth() != width) {
            i.image_ = Resize(i.image_, width, height, keepAspectRatio);
        }

        // Transpose image
        if (transpose) {
            i.image_ = Transpose(i.image_);
        }

        return ToImageBuffer(i, type);
    }
}
