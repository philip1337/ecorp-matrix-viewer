package util;

import picocli.CommandLine;
import java.util.Properties;

public class SimpleApp {
    /**
     * @param args
     * @return true if app is ready
     */
    public boolean Run(String[] args) {
        OnLoad();

        // Get options
        Object o = GetOptions();
        assert o != null : "Options are null";

        try {
            // Populate the created class from the command line arguments.
            CommandLine.populateCommand(o, args);

        } catch (CommandLine.ParameterException e) {
            // The given command line arguments are invalid, for example there
            // are options specified which do not exist or one of the options
            // is malformed (missing a value, for example).
            System.out.println(e.getMessage());

            CommandLine.usage(o, System.out);
            return false;
        }

        OnInit();
        OnApp();
        return true;
    }

    /**
     * Get options
     * @return
     */
    public Object GetOptions() {
        return null;
    }

    public void AppendLog(Object o, Properties p) {
        // TODO: Overwrite object values with propertie values
    }

    public void OnLoad() {}
    public void OnInit() {}
    public void OnApp() {}
}
