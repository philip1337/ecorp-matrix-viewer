package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                        description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-dlist", "--device-list" }, description = "Show device list.")
    public boolean showDevices_ = false;

    @CommandLine.Option(names = { "-r", "--ratio" }, description = "Keep aspect ratio")
    public boolean aspectRatio_ = false;

    @CommandLine.Option(names = { "-c", "--clean" }, description = "Clean matrix, vanish.")
    public boolean clean_ = false;

    @CommandLine.Option(names = { "-t", "--transpose" }, description = "Transpose matrix.")
    public boolean transpose_ = false;

    @CommandLine.Option(names = { "-x", "--width" }, description = "Width of the matrix (x, default: 16).")
    public int width_ = 16;

    @CommandLine.Option(names = { "-y", "--height" }, description = "Height of the matrix (y, default: 16).")
    public int height_ = 16;

    @CommandLine.Option(names = { "-s", "--s" }, description = "Service duration in seconds (default: 0 = permanent).")
    public long duration_ = 0;

    @CommandLine.Option(names = { "-fp", "--frame-pause" }, description = "Pause per frame (default: 100 ms)")
    public int pause_ = 100;

    @CommandLine.Option(names = { "-p", "--picture" }, description = "Picture to display on the matrix.")
    public String picture_ = "test.jpg";

    @CommandLine.Option(names = { "-d", "--device" }, description = "Select matrix device endpoint.")
    public String device_ = "Arrow USB Blaster";

    @CommandLine.Option(names = { "-b", "--brightness" }, description = "Brightness value (0-255, default: 50).")
    public float brightness_ = 0.06f;
}
