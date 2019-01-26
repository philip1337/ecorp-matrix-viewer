package app;

import fpga.Transmitter;
import fpga.Types;
import service.BroadcastService;
import service.MasterClientService;
import service.MasterDiscoveryService;
import util.ServiceManager;
import util.SimpleApp;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

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
     * Broadcast
     */
    private BroadcastService broadcast_ = null;

    /**
     * Master discovery
     */
    private MasterDiscoveryService discovery_ = null;

    /**
     * Master services...
     */
    private List<MasterClientService> masters_ = null;

    /**
     * Main master service
     */
    private MasterClientService master_ = null;

    /**
     * Transmitter
     */
    private Transmitter transmitter_ = null;

    /**
     * Entry point
     * @param args commandline
     */
    public static void main( String[] args) {
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
        // Option handler
        options_ = new Options();

        // Service manager
        services_ = new ServiceManager();

        // Master list
        masters_ = new ArrayList<>();
    }

    /**
     * OnInit
     */
    @Override
    public void OnInit() {
        // Transmitter
        transmitter_ = new Transmitter(options_.width_, options_.height_);

        // Register services
        try {
            RegisterServices();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point
     */
    @Override
    public void OnApp() {
        // Find matrix module
        byte ret = transmitter_.FindModules(options_.device_);
        if (ret != Types.READY) {
            System.out.printf("[Error] Failed to initialize matrix error code: %d. \n", ret);
            return;
        }

        // Register threaded services
        services_.Start();

        // Wait handler
        try {

            while(true) {
                // Try to connect if master is not ready
                if (master_ == null || !master_.Ready()) {
                    // Interrupt old service and restart connect process
                    if (master_ != null)
                        master_ .Interrupt();
                    master_ = null;

                    Connect();
                    Notify();
                }

                // Clean pending masters
                CleanMasters();
            }

            // Wait till we exit
            //services_.Wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify
     */
    private void Notify() {
        // Set transmitter
        master_.SetTransmitter(transmitter_);

        // Not connected
        if (master_ == null)
            return;

        // Notify
        master_.Notify(options_.width_, options_.height_);
    }

    /**
     * Master service
     */
    private void RegisterMasterClientService() {
        // Append default service
        if (options_.master_.length() > 0) {
            // Register to possible masters
            masters_.add(CreateService(options_.master_, options_.port_, true));
        }
    }

    /**
     * Create service
     * @param host ip
     * @param port number
     * @param reconnect true if service should reconnect
     * @return service
     */
    private MasterClientService CreateService(String host, int port, boolean reconnect) {
        // Master client services...
        final MasterClientService s = new MasterClientService(host, port);
        if (options_.ssl_)
            s.InitializeSSL();
        s.SetReconnect(reconnect);
        s.Start();
        return s;
    }

    /**
     * Connect to master server
     * @throws InterruptedException on interrupt
     */
    private void Connect() throws InterruptedException {
        // Register
        boolean register;

        // Master
        RegisterMasterClientService();

        // Connect loop
        while (true) {
            // Register
            register = false;

            // Check if discovery found a new address
            InetAddress addr = discovery_.PopAddress();

            // Check if one of the services is ready
            for (MasterClientService service : masters_) {
                // If service is not
                if ((service.isInterrupted() || !service.isAlive()) && service.Reconnect()) {
                    // Register a new one
                    masters_.add(CreateService(service.GetHost(), service.GetPort(), service.Reconnect()));

                    // Pause for atleast 6 seconds to avoid mass reconnects
                    Thread.sleep(1000 * 6);

                    // Remove the old one
                    masters_.remove(service);
                }

                // Addr check
                if (addr != null && !service.GetHost().equals(addr.getHostAddress())) {
                    register = true;
                }

                // If service is ready we quit.
                if (service.Ready()) {
                    // Register main service
                    master_ = service;

                    // Make sure our main service is out of the list
                    masters_.remove(service);
                    return;
                }
            }

            // Register
            if (register) {
                // Register to possibler masters
                final MasterClientService s = new MasterClientService(addr.getHostAddress(), options_.port_);
                if (options_.ssl_)
                    s.InitializeSSL();
                s.Start();
                masters_.add(s);
            }

            // Pause
            Thread.sleep(100);
        }
    }

    /**
     * Clean masters
     */
    private void CleanMasters() {
        for (MasterClientService service : masters_) {
            service.Interrupt();
        }
        masters_.clear();
    }

    /**
     * Register services
     * @throws SocketException if MasterDiscoveryService isn't ready
     */
    private void RegisterServices() throws SocketException {
        // If we support the discovery service....
        if (!options_.wdc_) {
            // Register broadcast channel
            broadcast_ = new BroadcastService(59607);
            discovery_ = new MasterDiscoveryService(broadcast_);

            // Set discovery service
            broadcast_.SetDiscoveryService(discovery_);

            // Register to main service handler
            services_.Register(broadcast_);
            services_.Register(discovery_);
        }
    }

}
