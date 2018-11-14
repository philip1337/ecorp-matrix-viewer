package util;

import java.util.ArrayList;
import java.util.List;

public class ServiceManager {
    /**
     * Services
     */
    private List<Thread> services_ = null;

    /**
     * Constructor
     */
    public ServiceManager() {
        services_ = new ArrayList<>();
    }

    /**
     * Register thread
     * @param service
     */
    public void Register(Thread service) {
        services_.add(service);
    }

    /**
     * Start threads
     */
    public void Start() {
        for(Thread r : services_) {
            r.Start();
        }
    }

    /**
     * Shutdown
     */
    public void Stop() {
        for(Thread r : services_) {
            r.interrupt();
        }
    }

    /**
     * Wait till threads are done
     * @throws InterruptedException
     */
    public void Wait() throws InterruptedException {
        for(Thread r : services_) {
            r.join();
        }
    }
}
