package app;

import picocli.CommandLine;

public class Options {
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true,
                        description = "Print usage help and exit.")
    public boolean usageHelpRequested;

    @CommandLine.Option(names = { "-x", "--xml" }, description = "XML Path")
    public String xmlPath_ = "files.xml";

    @CommandLine.Option(names = { "-a", "--archive" }, description = "Archive path")
    public String archivePath_ = "archive.big";

    @CommandLine.Option(names = { "-b", "--base" }, description = "Base archive path")
    public String base_ = "";

    @CommandLine.Option(names = { "-v", "--validate" }, description = "Validate file")
    public String validate_ = "";	
}
