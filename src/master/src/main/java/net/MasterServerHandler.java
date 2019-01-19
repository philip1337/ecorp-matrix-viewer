package net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import message.HeloMessage;
import message.Message;
import types.Client;
import types.Master;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
     * Constructor
     * @param list containing all nodes
     */
    public MasterServerHandler(List<Client> list) {
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

        // Add client
        Client c = new Client(
                ctx, msg.width, msg.height, msg.version_, msg.time_,
                msg.vmVersion_, msg.vmName_, msg.hostname_
        );

        // Register
        clients_.add(c);

        // Get mx data
        RuntimeMXBean b = ManagementFactory.getRuntimeMXBean();
        String hostname = "";

        // Try to get node hostname
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // Otherwise.... unknown
            hostname = "<Unknown>";
        }

        // Create Ehlo response
        Message m = new Message();
        m.header_ = Master.HEADER_MN_EHLO;
        m.object_ = NodePackets.CreateEhlo(Master.VERSION, Master.HEADER_MAGIC,
                                           b.getVmVersion(), b.getVmName(), hostname, b.getUptime());

        // Answer
        c.Write(m);
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

    /**
     * Read incoming stream
     * @param ctx channel
     * @param msg message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Message
        Message message = (Message)msg;

        // Header
        switch(message.header_) {
            case Master.HEADER_NM_HELO:
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

    /**
     * On read complete
     * @param ctx channel
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * On channel error (closed, disconnect etc.)
     * @param ctx channel
     * @param cause reason
     */
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
     * On channel exit
     * @param ctx channel
     * @throws Exception on error
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        RemoveClient(ctx);
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
     * @param header byte
     * @param o serializable object
     */
    public void SendToAll(int header, Object o) {
        Message msg = new Message();
        msg.header_ = header;

        // Packet data
        msg.object_ = o;

        WriteToAll(msg);
    }
}

