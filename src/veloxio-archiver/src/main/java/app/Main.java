package app;

import util.SimpleApp;

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
     * pPackPath
     */
    @Override
    public void OnLoad() {
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
        if (options_.validate_.length() == 0) {
			new ActionPack(options_.base_, options_.xmlPath_,
						   options_.archivePath_).Run();
        } else {
			new ActionValidate(options_.validate_,
						       options_.archivePath_).Run();
		}	
    }
}
