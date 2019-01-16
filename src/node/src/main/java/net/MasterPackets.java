package net;

import message.HeloMessage;


public class MasterPackets {
    public static HeloMessage CreateHelo(int version, String magic, int width,
                                         int height, long time, String vmVersion, String vmName, String hostname) {
        HeloMessage msg = new HeloMessage();
        msg.version_ = version;
        msg.magic_ = magic;
        msg.width = width;
        msg.height = height;
        msg.time_ = time;
        msg.vmVersion_ = vmVersion;
        msg.vmName_ = vmName;
        msg.hostname_ = hostname;
        return msg;
    }
}
