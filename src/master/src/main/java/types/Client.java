package types;

import io.netty.channel.ChannelHandlerContext;
import message.ImageMessage;
import message.Message;
import util.ImageLoader;

import java.awt.image.BufferedImage;
import java.io.File;

public class Client {
    private ChannelHandlerContext ctx_ = null;

    private int width_;
    private int height_;
    private int version_;

    private long time_;
    private String vmVersion_;
    private String vmName_;
    private String hostname_;

    /**
     * Client constructor
     * @param ctx channel
     * @param width width of the client matrix
     * @param height height of the client matrix
     * @param version version (we need this to make sure we handle each clients versions, different updates)
     */
    public Client(ChannelHandlerContext ctx, int width, int height, int version,
                  long time, String vmVersion, String vmName, String hostname) {
        ctx_ = ctx;
        width_ = width;
        height_ = height;
        version_ = version;
        time_ = time;
        vmVersion_ = vmVersion;
        vmName_ = vmName;
        hostname_ = hostname;
    }


    /**
     * Get channel
     * @return channel
     */
    public ChannelHandlerContext GetChannel() {
        return ctx_;
    }

    /**
     * Write and flush
     * @param o data
     */
    public void Write(Object o) {
        ctx_.writeAndFlush(o);
    }
}
