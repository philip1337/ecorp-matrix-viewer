package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
            description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-b", "--bind" }, description = "Bind IP address")
    public String bind_ = null;

    @CommandLine.Option(names = { "-p", "--port" }, description = "Bind Port (default: 50000")
    public int port_ = 50000;

    @CommandLine.Option(names = { "-bp", "--broadcast-port" }, description = "Bind Port (default: 50000")
    public int broadcast_ = 50001;

    @CommandLine.Option(names = { "-wdc", "--without-discovery" }, description = "Disable auto discovery service")
    public boolean wdc_ = false;

    @CommandLine.Option(names = { "-ssl", "--secure-socket-layer" }, description = "Enable ssl encryption")
    public boolean ssl_ = false;

    @CommandLine.Option(names = { "-a", "--assets" }, description = "Web asset path (veloxio archive)")
    public String assets_ = "web.big";

    @CommandLine.Option(names = { "-wp", "--web-port" }, description = "Web port (default: 8080")
    public int webPort_ = 8080;
}
