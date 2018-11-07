package service;

import net.BroadcastSocket;
import types.Packets;

public class BroadcastService extends BroadcastSocket {
    /**
     * Constructor
     *
     * @param port
     */
    BroadcastService(int port) {
        super(port);

    }

    @Override
    public void HandlePacket(byte header, byte[] data) {
        switch(header) {
            case 0:
                break;
        }
    }
}
