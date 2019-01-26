package types;


import java.awt.image.BufferedImage;

public class ImageFrame {
    public BufferedImage image_ = null;
    public int delay_ = 0;
    private String disposal_;

    public ImageFrame() {

    }

    public ImageFrame(BufferedImage image, int delay, String disposal) {
        this.image_ = image;
        this.delay_ = delay;
        this.disposal_ = disposal;
    }

    public BufferedImage getImage() {
        return image_;
    }

    public int getDelay() {
        return delay_;
    }

    public String getDisposal() {
        return disposal_;
    }
}
