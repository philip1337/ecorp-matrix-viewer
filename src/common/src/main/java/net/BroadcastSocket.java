package net;

import util.Thread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastSocket extends Thread {
    private DatagramSocket socket = null;
    byte[] buf = new byte[256];

    /**
     * Thread handler
     */
    public void Run() {
        try {
            socket = new DatagramSocket(59685);
            socket.setBroadcast(true);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.printf("%s\n", packet.getAddress());
                String received = new String(
                        packet.getData(), 0, packet.getLength());
                System.out.printf("%s\n", received);
                if ("end".equals(received)) {
                    break;
                }
            }

        } catch (IOException e) {
            // TODO: Error handler
            e.printStackTrace();
        }
        System.out.println("socket");
    }
}

