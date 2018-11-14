package net;

import types.Broadcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

public class BroadcastPackets {
    public static void SendEhlo(DatagramSocket socket, InetAddress address, int port) throws IOException {
        // Get public ip list
        List<InetAddress> list = Interfaces.GetPublic();

        // Allocate ip buffer
        ByteBuffer buffer = ByteBuffer.allocate(1+(list.size() * 4));

        // Header
        buffer.put(Broadcast.HEADER_MN_EHLO);

        // Data
        for(InetAddress addr : list) {
            // We just support ipv4 broadcast discovery currently
            if (addr.getAddress().length == 4)
                buffer.put(addr.getAddress());
        }

        // Notify over broadcast
        DatagramPacket data = new DatagramPacket(buffer.array(), buffer.array().length, address, port);
        socket.send(data);
    }
}
