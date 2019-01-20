package app;

import service.BroadcastService;
import service.MasterServerService;
import service.WebServerService;
import util.ServiceManager;
import util.SimpleApp;
import veloxio.Provider;

import java.io.FileNotFoundException;
import java.net.SocketException;

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
     * Broadcast service
     */
    private BroadcastService broadcast_ = null;

    /**
     * Master service
     */
    private MasterServerService master_ = null;

    /**
     * Web service
     */
    private WebServerService web_ = null;

    /**
     * VeloxIO Data provider
     */
    private Provider provider_ = null;

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
     * Is dev
     * @return true if is running in dev or ide
     */
    private boolean IsDev() {
        String classPath = System.getProperty("java.class.path");
        return classPath.contains("idea_rt.jar");
    }

    /**
     * OnLoad
     */
    @Override
    public void OnLoad() {
        // Options
        options_ = new Options();

        // Service manager
        services_ = new ServiceManager();

        // VeloxIO - web asset provider
        provider_ = new Provider(IsDev(), "src/master-web-ui/");
    }

    /**
     * Entry point
     */
    @Override
    public void OnInit() {
        // Developer path
        if (IsDev())
            options_.assets_ = "src/master/build/libs/web.big";

        // Try to register web asset archive
        try {
            provider_.RegisterArchive(options_.assets_);
        } catch (FileNotFoundException e) {
            // TODO: Log
            System.out.printf("Failed: %s\n", e.getMessage());
        }

        // Register services
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
        web_ = new WebServerService(options_.webPort_, provider_, master_.GetClients());

        // Initialize broadcast service
        try {
            broadcast_ = new BroadcastService(options_.broadcast_);
            services_.Register(broadcast_);
        } catch (SocketException e) {
            // TODO: Log if failed
        }

        // Try to initialize ssl
        if (options_.ssl_)
            master_.InitializeSSL();

        // Register
        services_.Register(master_);
        services_.Register(web_);
    }
}
