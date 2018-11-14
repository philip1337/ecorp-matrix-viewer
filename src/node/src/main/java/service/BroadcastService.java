package service;

import net.BroadcastSocket;
import types.Broadcast;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class BroadcastService extends BroadcastSocket {
    /**
     * Master discovery service
     */
    private MasterDiscoveryService discovery_ = null;

    /**
     * Constructor
     *
     * @param port (used for listening on broadcast channel)
     */
    public BroadcastService(int port) throws SocketException {
        super(port);
        discovery_ = new MasterDiscoveryService(this);
    }

    /**
     * Get discovery service
     * @return discovery service
     */
    public MasterDiscoveryService GetDiscoveryService() {
        return discovery_;
    }

    /**
     * Handle packet
     * @param header byte
     * @param data buffer
     * @param address remote
     */
    @Override
    public void HandlePacket(byte header, ByteBuffer data, InetAddress address) {
        switch (header) {
            case Broadcast.HEADER_MN_EHLO:
                PacketEhlo(data, address);
                break;
            default:
                System.out.printf("Invalid header: %d from %s\n", header, address.getHostAddress());
        }
    }

    /**
     * Packet ehlo
     * @param data
     * @param address
     */
    private void PacketEhlo(ByteBuffer data, InetAddress address) {
        // Determine address length
        int addrLenght = address.getAddress().length;

        // address.getAddress().length -> should be 4 (ipv4)
        int n = data.limit() / address.getAddress().length;

        // Read ips
        byte[] buffer = new byte[address.getAddress().length];
        for (int i = 0; i < n; i++) {
            data.get(buffer, 0, addrLenght);
            try {
                discovery_.Add(InetAddress.getByAddress(buffer));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
