package app;

import com.fazecast.jSerialComm.SerialPort;
import fpga.Transmitter;
import fpga.Types;
import util.ImageLoader;
import util.SimpleApp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends SimpleApp {
    /**
     * Options
     */
    private Options options_ = null;

    /**
     * Entry point
     * @param args commandline
     */
    public static void main( String[] args) {
        new Main().Run(args);
    }

    /**
     * OnInit
     */
    @Override
    public void OnInit() {
        // Option handler
        options_ = new Options();
    }

    /**
     * Get options
     * @return
     */
    @Override
    public Object GetOptions() {
        return options_;
    }

    /**
     * Entry point
     */
    @Override
    public void OnApp() {
        // Transmitter
        Transmitter t = new Transmitter();

        // Show devices
        if (options_.showDevices_) {
            t.DumpModules();    // System output
            return;
        }

        // Find matrix module
        byte ret = t.FindModule(options_.device_);
        if (ret != Types.READY) {
            System.out.printf("[Error] Failed to initialize matrix error code: %d. \n", ret);
            return;
        }

        // Image buffer
        BufferedImage image = null;

        // Get image
        File f = new File(options_.picture_);
        if (!f.exists()) {
            System.out.printf("[Error] File not found: %s \n", f.getAbsolutePath());
            return;
        }

        // Image loader
        ImageLoader i = new ImageLoader();

        // Load image from file
        image = i.FromFile(f);

        // Image failed
        if (image == null) {
            System.out.printf("[Error] Failed to read image: %s. \n" , f.getAbsolutePath());
            return;
        }

        // TODO: TransmitImage should support != 16x16
        if (options_.width_ != 16 || options_.height_ != 16) {
            System.out.println("[Warning] We just support 16x16 currently, skipping transmission.");
            return;
        }

        // Transmit image to matrix
        try {
            // Info message
            System.out.printf("[Info] Brightness: %f Size: %dx%d - picture: %s \n", options_.brightness_, options_.width_, options_.height_, f.getAbsolutePath());

            // Resize image
            image = i.Resize(image, options_.width_, options_.height_);

            // Transmit
            t.TransmitImage(image, options_.brightness_);
        } catch (IOException e) {
            System.out.printf("[Error] Failed to transmit image error message: %s \n", e.getMessage());
        }
    }
}
