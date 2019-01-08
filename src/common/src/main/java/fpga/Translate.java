package fpga;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Translate {
    /**
     * Get image buffer
     * @param img Buffer
     * @return ByteBuffer with translated image for the rgb matrix
     */
    public ByteBuffer Image(BufferedImage img) {
        // Translate image
        int height = img.getHeight();
        int width = img.getWidth();
        int size = height * width * (Integer.SIZE / 8);

        // Buffer
        ByteBuffer buffer = ByteBuffer.allocate(size);

        // Loop trough stuff
        for (int y = 0; y < height; y++) {
            if (y % 2 != 0) {
                for (int x = 0; x < width; x++) {
                    buffer.putInt(img.getRGB(x, y));
                }
            } else {
                for (int x = width - 1; x > 0; x--) {
                    buffer.putInt(img.getRGB(x, y));
                }
            }
        }

        return buffer;
    }

    /**
     * Image to uart
     * @param image
     * @return
     */
    public ByteBuffer ImageToUart(BufferedImage image) {
        ByteBuffer b = Image(image);
        return ByteBuffer.wrap(String.format("<01%s>", b.toString()).getBytes());
    }
}
