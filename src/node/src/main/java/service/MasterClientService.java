package service;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.MasterClientHandler;
import util.Thread;

import javax.net.ssl.SSLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MasterClientService extends Thread {
    /**
     * SSL Context
     */
    private SslContext ctx_ = null;

    /**
     * Workers
     */
    private EventLoopGroup worker_ = null;

    /**
     * Host
     */
    private String host_ = "";

    /**
     * Port
     */
    private int port_ = 0;

    /**
     * Ready
     */
    private AtomicBoolean ready_ = new AtomicBoolean(false);

    /**
     * Constructor
     */
    public MasterClientService(String host, int port) {
        port_ = port;
        host_ = host;
        worker_ = new NioEventLoopGroup();
    }

    /**
     * Check if is ready
     * @return true if ready
     */
    public boolean Ready() {
        return ready_.get();
    }

    /**
     * Get host address
     * @return ip
     */
    public String GetHost() {
        return host_;
    }

    /**
     * Initialize ssl handler
     * @return true if ssl ready
     */
    public boolean InitializeSSL() {
        try {
            ctx_ = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            // TODO: Log
            return false;
        }
        return true;
    }

    /**
     * Run
     */
    public void run() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(worker_)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    if (ctx_ != null) {
                        p.addLast(ctx_.newHandler(ch.alloc(), host_, port_));
                    }

                    p.addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                        new MasterClientHandler()
                    );
                }
            });

            // Start the connection attempt.
            ChannelFuture f = b.connect(host_, port_);
            if (f == null)
                return;

            // Ready
            ready_.set(true);

            // Sync and load channel
            f.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO: Log
            e.printStackTrace();
            ready_.set(false);
        } finally {
            worker_.shutdownGracefully();
            ready_.set(false);
        }
    }
}
