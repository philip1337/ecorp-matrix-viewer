package util;

public class Color extends java.awt.Color {
    /**
     * Brightness
     */
    private float brightness_ = 1;

    /**
     * Constructor
     * @param r
     * @param g
     * @param b
     */
    public Color(int r, int g, int b) {
        super(r, g, b);
    }

    /**
     * Constructor
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public Color(int r, int g, int b, int a) {
        super(r,g,b,a);
    }

    /**
     * Constructor
     * @param rgb
     */
    public Color(int rgb) {
        super(rgb);
    }

    /**
     * To bytes
     * @return color as byte array
     */
    public byte[] ToBytes() {
        return String.format("%02X%02X%02X",
                Math.max((int)(getRed() * brightness_), 0),
                Math.max((int)(getGreen() * brightness_), 0),
                Math.max((int)(getBlue() * brightness_), 0)).getBytes();
    }

    /**
     * Adjust brightness
     * @param brightness float value 1 = max
     */
    public void SetBrightness(float brightness) {
        brightness_ = brightness;
    }
}
