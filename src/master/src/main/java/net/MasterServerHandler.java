package net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import message.HeloMessage;
import message.Message;
import types.Client;
import types.Master;

import java.util.List;

/**
 * MasterServerHandler
 */
public class MasterServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * Clients
     */
    private List<Client> clients_ = null;

    /**
     * Client list
     * @param list containing clients
     */
    public void SetClientList(List<Client> list) {
        clients_ = list;
    }

    /**
     * Register client
     * @param ctx channel
     * @param msg helo message data (containing infos about the client)
     */
    private void RegisterClient(ChannelHandlerContext ctx, HeloMessage msg) {
        // If channel is already registered we skip it
        for(Client c : clients_) {
            if (c.GetChannel() == ctx)
                return;
        }

        // Register
        clients_.add(new Client(
                ctx, msg.width, msg.height, msg.version_, msg.time_,
                msg.vmVersion_, msg.vmName_, msg.hostname_
        ));
    }

    /**
     * Remove client
     * @param ctx channel
     */
    private void RemoveClient(ChannelHandlerContext ctx) {
        // If channel is already registered we skip it
        for(Client c : clients_) {
            if (c.GetChannel() == ctx)
                clients_.remove(c);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Message
        Message message = (Message)msg;

        // Header
        switch(message.header_) {
            case Master.HEADER_MN_HELO:
                // Validate object type
                if (message.object_.getClass() != HeloMessage.class)
                    return;

                // Cast message
                HeloMessage helo = (HeloMessage)message.object_;

                // If magic is invalid
                if (!helo.magic_.equals(Master.HEADER_MAGIC))
                    return;

                // Register channel
                RegisterClient(ctx, helo);
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
        // First unregister
        RemoveClient(ctx);

        // TODO: Log
        cause.printStackTrace();

        // Close channel
        ctx.close();
    }

    /**
     * Write
     * @param msg object
     */
    private void WriteToAll(Object msg) {
        for (Client ctx : clients_) {
            ctx.Write(msg);
        }
    }

    /**
     * Send to all clients
     * @param header
     * @param o
     */
    public void SendToAll(int header, Object o) {
        Message msg = new Message();
        msg.header_ = header;

        // Packet data
        msg.object_ = o;

        WriteToAll(msg);
    }
}

