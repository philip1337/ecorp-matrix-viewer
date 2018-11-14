package net;

import util.Thread;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
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
    protected int port_ = 0;

    /**
     * Constructor
     * @param port
     */
    public BroadcastSocket(int port) throws SocketException {
        port_ = port;
        InitializeSocket();
    }

    /**
     * Initialize socket
     * @throws SocketException
     */
    protected void InitializeSocket() throws SocketException {
        assert(socket_ == null);
        socket_ = new DatagramSocket(port_);
    }

    public DatagramSocket GetSocket() {
        assert(socket_ != null);
        return socket_;
    }

    /**
     * Packet handler
     * @param header byte
     * @param data buffer
     * @param address remote
     */
    public void HandlePacket(byte header, ByteBuffer data, InetAddress address) {}

    /**
     * Thread handler (run method)
     */
    public void run() {
        try {
            while (true) {
                // Read packet
                DatagramPacket packet = new DatagramPacket(buffer_, buffer_.length);
                socket_.receive(packet);

                // Invalid packet size
                int length = packet.getLength();
                if (length > 256 || length <= 0) {
                    continue;
                }

                // Header
                byte header = packet.getData()[0];

                // Get content
                ByteBuffer buffer = ByteBuffer.allocate(packet.getLength() - 1);
                buffer.put(Arrays.copyOfRange(packet.getData(), 1, packet.getLength()));
                buffer.flip();

                // Handle packet
                HandlePacket(header, buffer, packet.getAddress());
            }

        } catch (IOException e) {
            // TODO: Error handler
            e.printStackTrace();
        }
    }
}

