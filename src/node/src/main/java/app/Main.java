package app;


import service.BroadcastService;
import service.MasterDiscoveryService;
import types.Broadcast;
import util.ServiceManager;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Main {
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
    public static void main( String[] args )
    {
        try {
            new Main().Run();
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

    /**
     * Run
     */
    public void Run() throws InterruptedException {
        try {
            RegisterServices();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Register threaded services
        services_.Start();

        // Wait handler
        services_.Wait();
    }
}
