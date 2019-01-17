package net;

import message.EhloMessage;

public class NodePackets {
    public static EhloMessage CreateEhlo(int version, String magic,
                                         String vmVersion, String vmName, String hostname, long serverTime) {
        EhloMessage msg = new EhloMessage();
        msg.version_ = version;
        msg.magic_ = magic;
        msg.serverTime_ = serverTime;
        msg.vmVersion_ = vmVersion;
        msg.vmName_ = vmName;
        msg.hostname_ = hostname;
        return msg;
    }
}
