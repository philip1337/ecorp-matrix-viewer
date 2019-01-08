package app;

import fpga.Translate;
import fpga.Transmitter;
import fpga.Types;
import util.ConfigReader;
import util.ImageLoader;
import util.SimpleApp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Main
 */
public class Main extends SimpleApp {
    /**
     * Options
     */
    Options options_ = null;

    /**
     * Config
     */
    ConfigReader cfg_ = null;

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {
        new Main().Run(args);
    }

    /**
     * OnInit
     */
    @Override
    public void OnInit() {
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

    }
}
