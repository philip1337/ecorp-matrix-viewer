package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                        description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-c", "--config" }, description = "Config file path relative to the working directory or absolute.")
    public String config_ = "config.properties";

    @CommandLine.Option(names = { "-m", "--master" }, description = "Master server IP address.")
    public String master_ = "";

    @CommandLine.Option(names = { "-p", "--port" }, description = "Master server port.")
    public int port_ = 50000;

    @CommandLine.Option(names = { "-wdc", "--without-discovery" }, description = "Disable discovery service")
    public boolean wdc_ = false;

    @CommandLine.Option(names = { "-ssl", "--secure-socket-layer" }, description = "Enable ssl encryption")
    public boolean ssl_ = false;
}
