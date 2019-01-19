package net;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;

import io.netty.util.internal.SystemPropertyUtil;
import types.WebRouteTable;
import types.WebSession;
import veloxio.ArchiveFile;
import veloxio.Provider;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class WebServerHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * Buffers
     */
    private Map<ChannelHandlerContext, WebSession> sessions_;

    /**
     * Router
     */
    private WebRouteTable router_ = null;

    /**
     * Asset provider
     */
    private Provider provider_ = null;

    /**
     * Constructor
     * @param router to route web requests
     */
    public WebServerHandler(WebRouteTable router, Provider provider) {
        router_ = router;
        provider_ = provider;
        sessions_ = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        sessions_.put(ctx, new WebSession());
    }

    /**
     * Read finished
     * @param ctx channel
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * Read channel
     * @param ctx channel
     * @param msg message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // Get session
        WebSession s = sessions_.get(ctx);

        // Should not happen
        if (s == null)
            return;

        // Simple http request
        if (msg instanceof HttpRequest) {
            // Reset content
            s.Reset();

            // Cast request
            HttpRequest request = s.request_ = (HttpRequest) msg;

            // Check if channel is 100 continue
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            // If post we need the post request decoder
            if (request.method() == HttpMethod.POST) {
                s.decoder_ = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                s.decoder_.setDiscardThreshold(0);
            }

            // Find route
            s.route_ = router_.FindRoute(request.method(), request.uri());

            // If route is not found maybe it is an asset...
            if (s.route_ == null && request.method() == HttpMethod.GET) {
                try {
                    // Get file from archive
                    s.dataBuffer_ = provider_.Get(request.uri());
                } catch (IOException e) {}
            }

            // If we cant find both... we respond with a 404
            if (s.route_ == null && s.dataBuffer_ == null) {
                // We try 2 go for the 404
                s.route_ = router_.FindRoute(HttpMethod.GET, "/404");

                // Fatal btw... (404 should always be an existent route...)
                if (s.route_ == null) {
                    s.buffer_.append("Error, server not ready (fatal!)!\r\n");
                    s.buffer_.append("===================================\r\n");
                    s.buffer_.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
                    s.buffer_.append("HOSTNAME: ").append(request.headers()
                             .get(HttpHeaderNames.HOST, "unknown")).append("\r\n");
                    s.buffer_.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");
                }
            }
        } else if (msg instanceof HttpContent) {
            // Content
            HttpContent httpContent = (HttpContent) msg;

            // Decoder (request body)
            if (s.decoder_ != null && s.route_ != null && s.route_.SupportsPost()) {
                // Offer
                s.decoder_.offer(httpContent);

                // Read chunk
                try {
                    ReadChunk(ctx, s);
                } catch (IOException e) {
                    // TODO: Log
                }
            }

            // End
            if (msg instanceof LastHttpContent) {
                LastHttpContent trailer = (LastHttpContent) msg;
                if (!WriteResponse(trailer, ctx, s)) {
                    // If keep-alive is off, close the connection once the content is fully written.
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    /**
     * Read chunk
     * @param ctx channel
     * @param s WebSessions containing decoder and route
     * @throws IOException on io error
     */
    private void ReadChunk(ChannelHandlerContext ctx, WebSession s) throws IOException {
        while (s.decoder_.hasNext()) {
            InterfaceHttpData data = s.decoder_.next();
            if (data != null) {
                try {
                    switch (data.getHttpDataType()) {
                        case Attribute:
                            final Attribute attr = (Attribute) data;
                            s.AddAttribute(attr);
                            break;
                        case FileUpload:
                            final FileUpload upload = (FileUpload) data;
                            s.AddFile(upload);
                            break;
                    }
                } finally {
                    data.release();
                }
            }
        }
    }

    /**
     * Write response
     * @param currentObj object
     * @param ctx channel
     * @param s session
     * @return true if keepAlive
     */
    private boolean WriteResponse(HttpObject currentObj, ChannelHandlerContext ctx, WebSession s) {
        // Process request
        if (s.route_ != null)
            s.route_.Process(s.buffer_, s.request_, s);

        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(s.request_);

        // If data buffer we go for a simple response
        if (s.dataBuffer_ != null) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            HttpUtil.setContentLength(response, s.dataBuffer_.length);

            // Keep alive 1.1
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            // Get mime type
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(s.request_.uri()));

            // Response
            ctx.write(response);

            // File
            ctx.write(new DefaultHttpContent(Unpooled.copiedBuffer(s.dataBuffer_)));

            // Last content
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            // Build the response object.
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                    Unpooled.copiedBuffer(s.buffer_.toString(), CharsetUtil.UTF_8)
            );

            // Set content type
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                    s.route_ != null ? s.route_.type_ : "text/plain; charset=UTF-8"
            );

            // Keep alive 1.1
            if (keepAlive) {
                // Add 'Content-Length' header only for a keep-alive connection.
                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                // Add keep alive header as per:
                // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            ctx.write(response);
        }
        return keepAlive;
    }

    /**
     * Send continue
     * @param ctx channel
     */
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    /**
     * Exception caught
     * @param ctx channel
     * @param cause reason
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Drop
        sessions_.remove(ctx);
        ctx.close();
    }

    /**
     * Inactive
     * @param ctx channel
     * @throws Exception on error
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Drop
        sessions_.remove(ctx);
    }
}