package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                        description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-w", "--width" }, description = "Width of the matrix.")
    public int width_ = 16;

    @CommandLine.Option(names = { "-h", "--height" }, description = "Height of the matrix.")
    public int height_ = 16;

    @CommandLine.Option(names = { "-p", "--picture" }, description = "Picture to display on the matrix.")
    public String picture_ = "test.jpg";
}
