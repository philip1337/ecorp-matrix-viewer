package net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import message.HeloMessage;
import message.Message;
import types.Master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MasterClientHandler extends ChannelInboundHandlerAdapter {
    private List<ChannelHandlerContext> channels_ = null;

    /**
    * Creates a client-side handler.
    */
    public MasterClientHandler() {
        // Clear list
        channels_ = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channels_.add(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Message
        Message message = (Message)msg;

        // Header
        switch(message.header_) {
            case Master.HEADER_MN_IMAGE:
                System.out.printf("we got here something");
                break;
            default:
                // TODO: Log
                System.out.printf("Invalid header: %d\n", message.header_);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

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
        msg.header_ = Master.HEADER_MN_HELO;

        // Packet data
        msg.object_ = o;

        Write(msg);
    }

    /**
     * Ready
     * @return true if channel is ready
     */
    public boolean Ready() {
        return channels_.size() > 0;
    }
}
