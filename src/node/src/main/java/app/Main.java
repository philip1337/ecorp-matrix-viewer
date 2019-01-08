package app;


import service.BroadcastService;
import service.MasterDiscoveryService;
import types.Broadcast;
import util.ServiceManager;
import util.SimpleApp;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Main extends SimpleApp {
    /**
     * Options
     */
    Options options_ = null;

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
     * Entry point
     * @param args commandline
     */
    public static void main( String[] args) {
        new Main().Run(args);
    }

    /**
     * OnInit
     */
    @Override
    public void OnInit() {
        // Option handler
        options_ = new Options();

        // Service manager
        services_ = new ServiceManager();

        // Register services
        try {
            RegisterServices();
        } catch (SocketException e) {
            e.printStackTrace();
        }
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
        // Register threaded services
        services_.Start();

        // Wait handler
        try {
            services_.Wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Register services
     * @throws SocketException
     */
    private void RegisterServices() throws SocketException {
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
