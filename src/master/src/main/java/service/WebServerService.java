package service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import net.WebServerHandler;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import route.*;
import types.WebRouteTable;
import util.Thread;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class WebServerService extends Thread {
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
     * Web server port
     */
    private int port_ = 0;

    /**
     * Router
     */
    private WebRouteTable router_ = null;

    /**
     * Constructor
     */
    public WebServerService(int port) {
        boss_ = new NioEventLoopGroup(1);
        worker_ = new NioEventLoopGroup();
        port_ = port;
        router_ = new WebRouteTable();

        InitializeRoutes();
    }

    /**
     * Initialize routes
     */
    private void InitializeRoutes() {
        router_.AddRoute(new Index(HttpMethod.GET, "/"));
        router_.AddRoute(new Informations(HttpMethod.GET, "/informations"));

        router_.AddRoute(new Color(HttpMethod.POST, "/action/color"));
        router_.AddRoute(new Picture(HttpMethod.POST, "/action/picture"));
        router_.AddRoute(new RemotePicture(HttpMethod.POST, "/action/remote"));

        // Backup route
        router_.AddRoute(new NotFound(HttpMethod.GET, "/404"));
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
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (ctx_ != null) {
                            p.addLast(ctx_.newHandler(ch.alloc()));
                        }
                        p.addLast(new HttpRequestDecoder());
                        p.addLast(new HttpResponseEncoder());
                        p.addLast(new WebServerHandler(router_));
                    }
                });

        // Bind and start to accept incoming connections.
        try {
           Channel ch = b.bind(port_).sync().channel();
           ch.closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO: Log
            e.printStackTrace();
        } finally {
            boss_.shutdownGracefully();
            worker_.shutdownGracefully();
        }
    }

}
