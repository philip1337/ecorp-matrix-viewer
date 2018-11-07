package app;

import net.BroadcastSocket;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BroadcastSocket test = new BroadcastSocket();
        test.run();
        test.join();
        System.out.println("Test");
    }
}
