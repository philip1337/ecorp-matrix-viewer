package net;

import fpga.DisplayService;
import fpga.Transmitter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import message.ColorMessage;
import message.EhloMessage;
import message.ImageMessage;
import message.Message;
import types.Master;
import util.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MasterClientHandler extends ChannelInboundHandlerAdapter {
    /**
     * Master channels (our service can listen to multiple master servers)
     */
    private List<ChannelHandlerContext> channels_ = null;

    /**
     * Transmitter
     */
    private Transmitter transmitter_ = null;

    /**
     * Display service
     */
    private DisplayService display_ = null;

    /**
    * Creates a client-side handler.
    */
    public MasterClientHandler() {
        // Clear list
        channels_ = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Set transmitter
     * @param transmitter transmitter
     */
    public void SetTransmitter(Transmitter transmitter) {
        transmitter_ = transmitter;
    }

    /**
     * Channel active (ready)
     * @param ctx channel
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channels_.add(ctx);
    }

    /**
     * Stop display
     */
    private void StopDisplay() {
        // Wait till old display is done
        if (display_ != null) {
            display_.StopDisplay();
            try {
                display_.join();
            } catch (InterruptedException e) {
                display_ = null;
            }
        }
    }

    /**
     * Channel read
     * @param ctx channel
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Message
        Message message = (Message)msg;

        // Header
        switch(message.header_) {
            case Master.HEADER_MN_EHLO:
                EhloMessage m = (EhloMessage)message.object_;
                //TODO: Log master response...
                break;

            case Master.HEADER_MN_IMAGE:
                // Make sure we have no running display
                StopDisplay();

                // Setup display service
                ImageMessage i = (ImageMessage)message.object_;
                display_ = new DisplayService(transmitter_, i.duration_, i.brightness_);
                display_.SetFramesFromBuffer(i.image_, !i.processed_, i.keepAspectRatio_, i.transpose_);
                display_.SetPause(i.pause_);
                display_.Start();
                break;

            case Master.HEADER_MN_COLOR:
                // Make sure we have no running display
                StopDisplay();

                ColorMessage c = (ColorMessage)message.object_;

                // Setup display service
                display_ = new DisplayService(transmitter_, c.duration_, c.brightness_);
                display_.SetColor(c.r_, c.g_, c.b_);
                display_.Start();
                break;

            default:
                // TODO: Log
                System.out.printf("Invalid header: %d\n", message.header_);
        }
    }

    /**
     * Read complete
     * @param ctx channel
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * On channel error
     * @param ctx channel
     * @param cause reason
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // TODO: Log
        //cause.printStackTrace();
        ctx.close();
        channels_.remove(ctx);
    }

    /**
     * Write
     * @param msg object
     */
    private void Write(Object msg) {
        for (ChannelHandlerContext ctx : channels_) {
            ctx.writeAndFlush(msg);
        }
    }

    /**
     * Send
     * @param header id
     * @param o data
     */
    public void Send(int header, Object o) {
        // Send mesasge
        Message msg = new Message();
        msg.header_ = header;

        // Packet data
        msg.object_ = o;

        Write(msg);
    }

    /**
     * Ready
     * @return true if one channel is ready
     */
    public boolean Ready() {
        return channels_.size() > 0;
    }
}
