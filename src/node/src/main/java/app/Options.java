package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                        description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-c", "--config" }, description = "Config file path relative to the working directory or absolute.")
    public String config_ = "config.properties";

    @CommandLine.Option(names = { "-m", "--master" }, description = "Master server IP address.")
    public String master_ = "127.0.0.1";

    @CommandLine.Option(names = { "-p", "--port" }, description = "Master server port.")
    public int port_ = 50000;

    @CommandLine.Option(names = { "-wdc", "--without-discovery" }, description = "Disable discovery service")
    public boolean wdc_ = false;

    @CommandLine.Option(names = { "-ssl", "--secure-socket-layer" }, description = "Enable ssl encryption")
    public boolean ssl_ = false;

    @CommandLine.Option(names = { "-x", "--width" }, description = "Width of the matrix (x, default: 16).")
    public int width_ = 16;

    @CommandLine.Option(names = { "-y", "--height" }, description = "Height of the matrix (y, default: 16).")
    public int height_ = 16;

    @CommandLine.Option(names = { "-d", "--device" }, description = "Select matrix device endpoint.")
    public String device_ = "Arrow USB Blaster";
}
