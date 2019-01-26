package fpga;

import types.ImageBuffer;
import types.ImageFrame;
import util.Color;
import util.ImageLoader;
import util.Thread;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisplayService extends Thread {
    /**
     * Frame list
     */
    private List<ImageFrame> frames_ = null;

    /**
     * Duration
     */
    private long duration_;

    /**
     * Brightness
     */
    private float brightness_;

    /**
     * Set transmitter
     */
    private Transmitter transmitter_ = null;

    /**
     * Color
     */
    private Color color_ = null;

    /**
     * Ready
     */
    private AtomicBoolean stop_ = new AtomicBoolean(false);

    /**
     * Image loader
     */
    private ImageLoader loader_ = null;

    /**
     * 100 ms per frame
     */
    private int pause_ = 100;

    /**
     * Transmitted
     */
    private boolean transmitted_;

    /**
     * Constructor
     * @param duration duration
     * @param brightness float
     */
    public DisplayService(Transmitter transmitter, long duration, float brightness) {
        transmitter_ = transmitter;
        frames_ = new ArrayList<>();
        duration_ = duration;
        brightness_ = brightness;
        loader_ = new ImageLoader();
        transmitted_ = false;
    }

    /**
     * Pause per frame
     * @param pause
     */
    public void SetPause(int pause) {
        pause_ = pause;
    }

    /**
     * Stop display
     */
    public void StopDisplay() {
        stop_.set(true);
    }

    /**
     * Set color
     * @param r red
     * @param g green
     * @param b blue
     */
    public void SetColor(int r, int g, int b) {
        color_ = new Color(r,g,b);
    }

    /**
     * Add frame
     * @param i buffer
     * @param process if pictures should be processed
     * @param keepAspectRatio if picture should keep aspect ratio
     * @param rotate if picture should be rotated
     */
    public void AddFrame(ImageFrame i, boolean process, boolean keepAspectRatio, boolean transpose, int rotate) {
        // Process
        if (process) {
            frames_.add(loader_.ProcessImage(i, transmitter_.GetWidth(),
                    transmitter_.GetHeight(), keepAspectRatio, transpose, rotate));
        } else {
            frames_.add(i);
        }
    }

    /**
     * Set frames
     * @param frames as buffer
     * @param keepAspectRatio if picture should keep aspect ratio
     * @param transpose if images has to be transposed
     * @param rotate if images has to be rotated
     */
    public void SetFramesFromBuffer(List<ImageBuffer> frames, boolean process, boolean keepAspectRatio, boolean transpose, int rotate) {
        for (ImageBuffer buffer : frames) {
            final ImageFrame t = loader_.FromBufferToFrame(buffer);

            // If image is invalid | TODO: Log
            if (t == null)
                continue;

            // Add frame
            AddFrame(t, process, keepAspectRatio, transpose, rotate);
        }
    }

    /**
     * Set frames
     * @param frames as ImageBuffer
     * @param keepAspectRatio if picture should keep aspect ratio
     * @param transpose if images has to be transposed
     * @param rotate if images has to be rotated
     */
    public void SetFrames(List<ImageFrame> frames, boolean process, boolean keepAspectRatio, boolean transpose, int rotate) {
        for (ImageFrame t : frames) {
            // If image is invalid | TODO: Log
            if (t == null)
                continue;

            // Add frame
            AddFrame(t, process, keepAspectRatio, transpose, rotate);
        }
    }

    public void Clear() {
        // Clear matrix
        Color c = new Color(0, 0, 0);
        try {
            transmitter_.TransmitColor(c, 0.0f);
        } catch (IOException e) {
            // TODO: Log (failed to clear)
        }
    }

    /**
     * Run
     */
    public void run() {
        // Add seconds
        LocalTime localTime_ = LocalTime.now().plusSeconds(duration_);

        try {
            // If we are done
            while (!LocalTime.now().isAfter(localTime_) || duration_ == 0) {
                // If we have to stop
                if (stop_.get())
                    break;

                // As long we just have one frame we scan skip
                if (transmitted_ && (frames_.size() == 1 || color_ != null))
                    continue;

                try {
                    // If we show frames
                    if (frames_.size() >= 0) {
                        for (ImageFrame i : frames_) {
                            transmitter_.TransmitImage(i.image_, brightness_);
                            java.lang.Thread.sleep(i.delay_ > 0 ? i.delay_ : pause_);
                        }
                    } else if (color_ != null) {
                        transmitter_.TransmitColor(color_, brightness_);
                        java.lang.Thread.sleep(pause_);
                    }
                } catch (IOException e) {
                    // TODO: Log
                }

                // Ok we're fine
                transmitted_ = true;
            }

            Clear();
        } catch (InterruptedException e) {
            Clear();
        }
    }
}
