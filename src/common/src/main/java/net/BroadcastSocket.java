package net;

import util.Thread;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class BroadcastSocket extends Thread {
    /**
     * Socket
     */
    private DatagramSocket socket_ = null;

    /**
     * Socket buffer
     */
    private byte[] buffer_ = new byte[Types.BUFFER_LENGTH];

    /**
     * Port
     */
    private int port_ = 0;

    /**
     * Constructor
     * @param port
     */
    public BroadcastSocket(int port) {
        port_ = port;
    }

    /**
     * Initialize socket
     * @throws SocketException
     */
    protected void InitializeSocket() throws SocketException {
        assert(socket_ == null);
        socket_ = new DatagramSocket(port_);
    }

    public void HandlePacket(byte header, byte[] data) {

    }

    /**
     * Thread handler
     */
    public void Run() {
        try {
            while (true) {
                // Read packet
                DatagramPacket packet = new DatagramPacket(buffer_, buffer_.length);
                socket_.receive(packet);

                // Invalid packet size
                int length = packet.getLength();
                if (length > 256 || length <= 0)
                    continue;

                byte[] buffer = new byte[packet.getLength()];
                buffer = packet.getData();

                // Read header
                byte header = buffer[0];

                // Handle packet
                HandlePacket(buffer[0], Arrays.copyOfRange(buffer,
                                                     packet.getLength() + 1,
                                                           packet.getLength()));
            }

        } catch (IOException e) {
            // TODO: Error handler
            e.printStackTrace();
        }
        System.out.println("socket");
    }
}

