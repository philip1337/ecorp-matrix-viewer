package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    /**
     * Properties
     */
    private Properties properties_ = null;

    /**
     * Path to the file
     */
    private String path_ = null;

    /**
     * Config reader
     * @param path to the file
     */
    public ConfigReader(String path) {
        path_ = path;
    }

    /**
     * Config loader
     * @return true if success
     */
    public boolean Load()  {
        InputStream i = null;

        // Input stream
        try {
            i = new FileInputStream(path_);
        } catch (FileNotFoundException e) {
            // TODO: Log
            return false;
        }


        // Load properties
        try {
            properties_.load(i);
        } catch (IOException e) {
            // TODO: Log
            return false;
        }

        return true;
    }

    /**
     * Get Property
     * @param id config key
     * @return value
     */
    public String GetProperty(String id) {
        return properties_.getProperty(id);
    }

    /**
     * Get config value as int
     * @param id config key
     * @return int value
     */
    public int GetPropertyAsInt(String id) {
        return Integer.parseInt(GetProperty(id));
    }
}
