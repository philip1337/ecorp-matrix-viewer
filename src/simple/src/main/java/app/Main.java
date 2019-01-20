package app;

import fpga.DisplayService;
import fpga.Transmitter;
import fpga.Types;
import util.ImageLoader;
import util.SimpleApp;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.util.List;

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
     * OnLoad
     */
    @Override
    public void OnLoad() {
        // Option handler
        options_ = new Options();
    }

    /**
     * Get options
     * @return options
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
        // TODO: TransmitImage should support != 16x16
        if (options_.width_ != 16 || options_.height_ != 16) {
            System.out.println("[Error] We just support 16x16 currently.");
            return;
        }

        // Transmitter
        Transmitter t = new Transmitter(options_.width_, options_.height_);

        // Show devices
        if (options_.showDevices_) {
            t.DumpModules();    // System output
            return;
        }

        // Find matrix module
        byte ret = t.FindModule(options_.device_);
        if (ret != Types.READY) {
            System.out.printf("[Error] Failed to initialize matrix error code: %d. \n", ret);
            //return;
        }

        // Display service
        DisplayService service = new DisplayService(t, options_.duration_, options_.brightness_);

        // Image loader
        ImageLoader loader = new ImageLoader();

        // Get image
        File f = new File(options_.picture_);
        if (!f.exists()) {
            System.out.printf("[Error] File not found: %s \n", f.getAbsolutePath());
            return;
        }

        InputStream is;
        String mimeType = null;
        try {
            is = new BufferedInputStream(new FileInputStream(f));
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Mime type not found
        if (mimeType == null || !mimeType.equals("image/gif")) {
            System.out.printf("[Error] Failed to read image: %s. \n" , f.getAbsolutePath());

            // Load image from file
            BufferedImage image = loader.FromFile(f);

            // Image failed
            if (image == null) {
                System.out.printf("[Error] Failed to read image: %s. \n" , f.getAbsolutePath());
                return;
            }

            service.AddFrame(image, true);
        } else if (mimeType.equals("image/gif")){
            try {
                service.SetFrames(loader.GetFrames(f), true);
            } catch (IOException e) {
                System.out.printf("[Error] Failed to read frames: %s. \n" , f.getAbsolutePath());
                return;
            }
        }

        // Start service
        service.Start();

        // Wait till we're done
        try {
            service.Join();
        } catch (InterruptedException e) {
            // Silent exit
        }
    }
}
