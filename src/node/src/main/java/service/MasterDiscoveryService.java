package service;

import util.Thread;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MasterDiscoveryService extends Thread {
    /**
     * Broadcast
     */
    private BroadcastService broadcast_ = null;

    /**
     * Master list containing possible master ip addresses
     */
    private List<InetAddress> masterList_ = null;

    /**
     * Broadcast
     * @param s service
     */
    public MasterDiscoveryService(BroadcastService s) {
        broadcast_ = s;
        masterList_ = Collections.synchronizedList(new ArrayList<InetAddress>());
    }

    /**
     * Add
     * @param addr inet
     */
    public void Add(InetAddress addr) {
        masterList_.add(addr);
    }

    /**
     * Pop addr
     */
    public InetAddress PopAddress() {
        if (masterList_.size() <= 0)
            return null;

        return masterList_.remove(0);
    }

    /**
     * Main thread handler
     */
    public void run() {
        while(true) {

        }
    }
}
