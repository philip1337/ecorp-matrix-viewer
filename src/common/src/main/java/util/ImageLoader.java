package util;

import org.imgscalr.Scalr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import types.ImageBuffer;
import types.ImageFrame;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class ImageLoader {
    /**
     * Read image from file
     *
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
     *
     * @param buffer byte array
     * @return image or null
     */
    public ImageFrame FromBuffer(byte[] buffer) {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
        return FromStream(stream);
    }

    /**
     * From buffer to frame
     *
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
     *
     * @param stream input stream
     * @return image or null
     */
    public ImageFrame FromStream(InputStream stream) {
        try {
            ImageFrame f = new ImageFrame();
            f.image_ = ImageIO.read(stream);
            return f;
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Input stream
     *
     * @param buffer file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(byte[] buffer) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(buffer)));
        Read(frames, reader);
        return frames;
    }

    /**
     * Try to read delay
     *
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
                String delay = ((IIOMetadataNode) node).getAttribute("delayTime");
                d = Integer.parseInt(delay) * 10;
                break;
            }
        }

        return Integer.max(d, 10);
    }

    /**
     * Get frames
     *
     * @param gif file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(File gif) throws IOException {
        return GetFrames(ImageIO.createImageInputStream(gif));
    }

    /**
     * Get frames from stream
     *
     * @param stream image input stream
     * @return list with imageframes
     * @throws IOException if stream is invalid
     */
    public ArrayList<ImageFrame> GetFrames(ImageInputStream stream) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(stream);
        Read(frames, reader);
        return frames;
    }

    /**
     * Read
     *
     * @param frames frame array
     * @param reader image reader (buffer)
     * @throws IOException if failed to read from buffer
     */
    private void Read(ArrayList<ImageFrame> frames, ImageReader reader) throws IOException {
        int width = -1;
        int height = -1;

        IIOMetadata metadata = reader.getStreamMetadata();
        if (metadata != null) {
            IIOMetadataNode globalRoot = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

            NodeList globalScreenDescriptor = globalRoot.getElementsByTagName("LogicalScreenDescriptor");

            if (globalScreenDescriptor != null && globalScreenDescriptor.getLength() > 0) {
                IIOMetadataNode screenDescriptor = (IIOMetadataNode) globalScreenDescriptor.item(0);

                if (screenDescriptor != null) {
                    width = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenWidth"));
                    height = Integer.parseInt(screenDescriptor.getAttribute("logicalScreenHeight"));
                }
            }
        }

        BufferedImage master = null;
        Graphics2D masterGraphics = null;

        for (int frameIndex = 0; ; frameIndex++) {
            BufferedImage image;
            try {
                image = reader.read(frameIndex);
            } catch (IndexOutOfBoundsException io) {
                break;
            }

            if (width == -1 || height == -1) {
                width = image.getWidth();
                height = image.getHeight();
            }

            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0");
            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            int delay = Integer.valueOf(gce.getAttribute("delayTime"));
            String disposal = gce.getAttribute("disposalMethod");

            int x = 0;
            int y = 0;

            if (master == null) {
                master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else {
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
                    Node nodeItem = children.item(nodeIndex);
                    if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                        NamedNodeMap map = nodeItem.getAttributes();
                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                    }
                }
            }
            masterGraphics.drawImage(image, x, y, null);

            BufferedImage copy = new BufferedImage(master.getColorModel(), master.copyData(null), master.isAlphaPremultiplied(), null);
            frames.add(new ImageFrame(copy, delay, disposal));

            if (disposal.equals("restoreToPrevious")) {
                BufferedImage from = null;
                for (int i = frameIndex - 1; i >= 0; i--) {
                    if (!frames.get(i).getDisposal().equals("restoreToPrevious") || frameIndex == 0) {
                        from = frames.get(i).getImage();
                        break;
                    }
                }

                master = new BufferedImage(from.getColorModel(), from.copyData(null), from.isAlphaPremultiplied(), null);
                masterGraphics = master.createGraphics();
                masterGraphics.setBackground(new Color(0, 0, 0, 0));
            } else if (disposal.equals("restoreToBackgroundColor")) {
                masterGraphics.clearRect(x, y, image.getWidth(), image.getHeight());
            }
        }
        reader.dispose();
    }


    /**
     * Input stream
     *
     * @param stream file
     * @return frames
     * @throws IOException if read fails
     */
    public ArrayList<ImageFrame> GetFrames(InputStream stream) throws IOException {
        ArrayList<ImageFrame> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(stream);
        Read(frames, reader);
        return frames;
    }

    /**
     * Resize a buffered image
     * https://stackoverflow.com/questions/9417356/bufferedimage-resize
     *
     * @param img  buffer
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
     *
     * @param image         buffer
     * @param scaledWidth   bew width
     * @param scaledHeight  new height
     * @param preserveRatio keep aspect ratio
     * @return scaled image
     */
    public static BufferedImage Resize(BufferedImage image, int scaledWidth, int scaledHeight, boolean preserveRatio) {
        // Use scalr image library if we have to keep aspect ratio
        if (preserveRatio) {
            if (image.getHeight() >= image.getWidth()) {
                return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, scaledHeight);
            } else {
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
     *
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
     * Rotate
     *
     * @param image
     * @param rotate angle
     * @return
     */
    private BufferedImage Rotate(BufferedImage image, int rotate) {
        double rads = Math.toRadians(rotate);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, newWidth - 1, newHeight - 1);
        g2d.dispose();
        return rotated;
    }

    /**
     * Process image
     *
     * @param i               BufferedImage
     * @param width           expected width
     * @param height          expected height
     * @param keepAspectRatio keep aspect ratio
     * @param transpose       transpose image
     * @param rotate          angle
     * @return image buffer
     */
    public ImageFrame ProcessImage(ImageFrame i, int width, int height,
                                   boolean keepAspectRatio, boolean transpose, int rotate) {
        // Copy
        BufferedImage temp = i.image_;

        // Rotate frame
        if (rotate > 0) {
            temp = Rotate(temp, rotate);
        }

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
     *
     * @param i    image
     * @param type type (gif, jpg, png whatever)
     * @return buffer
     */
    public ImageBuffer ToImageBuffer(ImageFrame i, String type) {
        ImageBuffer ret = new ImageBuffer();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(i.image_, type, stream);
            stream.flush();
            ret.image_ = stream.toByteArray();
            stream.close();
        } catch (IOException ignored) {
        }

        ret.delay_ = i.delay_;
        return ret;
    }

    /**
     * Process image
     *
     * @param i               BufferedImage
     * @param width           expected width
     * @param height          expected height
     * @param type            image type png, jpg etc.
     * @param keepAspectRatio keep aspect ratio
     * @param transpose       transpose image
     * @param rotate          angle
     * @return transport array
     */
    public ImageBuffer ProcessImage(ImageFrame i, int width, int height,
                                    String type, boolean keepAspectRatio, boolean transpose, int rotate) {
        // Rotate frame
        if (rotate > 0) {
            i.image_ = Rotate(i.image_, rotate);
        }

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
