package app;

import service.MasterServerService;
import util.ConfigReader;
import util.ServiceManager;
import util.SimpleApp;

/**
 * Main
 */
public class Main extends SimpleApp {
    /**
     * Options
     */
    private Options options_ = null;

    /**
     * Services
     */
    private ServiceManager services_ = null;

    /**
     * Master service
     */
    private MasterServerService master_ = null;

    /**
     * Config
     */
    private ConfigReader cfg_ = null;

    /**
     * Main
     * @param args
     */
    public static void main(String[] args) {
        new Main().Run(args);
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
     * OnLoad
     */
    @Override
    public void OnLoad() {
        options_ = new Options();

        // Service manager
        services_ = new ServiceManager();
    }

    /**
     * Entry point
     */
    @Override
    public void OnInit() {
        RegisterServices();
    }

    /**
     * Entry point
     */
    @Override
    public void OnApp() {
        // Register threaded services
        services_.Start();

        // Wait handler
        try {
            services_.Wait();
        } catch (InterruptedException e) {
            // Silent exit
        }
    }

    /**
     * Register services
     */
    private void RegisterServices() {
        // Master service
        master_ = new MasterServerService(options_.port_);

        // Try to initialize ssl
        if (options_.ssl_)
            master_.InitializeSSL();

        // Register
        services_.Register(master_);
    }
}
