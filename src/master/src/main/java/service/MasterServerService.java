package service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import net.MasterServerHandler;
import util.Thread;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class MasterServerService extends Thread {
    /**
     * SSL Context
     */
    private SslContext ctx_ = null;

    /**
     * Workers
     */
    private EventLoopGroup boss_ = null;
    private EventLoopGroup worker_ = null;

    /**
     * Port
     */
    private int port_ = 0;

    /**
     * Constructor
     */
    public MasterServerService(int port) {
        port_ = port;
        boss_ = new NioEventLoopGroup(1);
        worker_ = new NioEventLoopGroup();
    }

    /**
     * Initialize ssl handler
     * @return
     */
    public boolean InitializeSSL() {
        SelfSignedCertificate ssc = null;
        try {
            ssc = new SelfSignedCertificate();
            ctx_ = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (CertificateException e) {
            // TODO: Log
            return false;
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
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss_, worker_)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                // If we support ssl
                if (ctx_ != null) {
                    p.addLast(ctx_.newHandler(ch.alloc()));
                }

                // Register encoders
                p.addLast(
                    new ObjectEncoder(),
                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    new MasterServerHandler()
                );
            }
        });

        // Bind and start to accept incoming connections.
        try {
            b.bind(port_).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO: Log
            e.printStackTrace();
        }
    }
}
