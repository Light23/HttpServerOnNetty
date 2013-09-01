package httpserv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.net.SocketAddress;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final ByteBuf CONTENT =
            Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World!!", CharsetUtil.US_ASCII));

    private static final AtomicInteger OPENED_CONNECTIONS = new AtomicInteger(0),
            TOTAL_CONNECTIONS = new AtomicInteger(0);

    private static final Map<String, AtomicInteger> IP_COUNT = new ConcurrentHashMap<String, AtomicInteger>(),
            REDIRECTS = new ConcurrentHashMap<String, AtomicInteger>();

    private static final List<Connection> CONNECTIONS = Collections.synchronizedList(new ArrayList<Connection>());

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        TOTAL_CONNECTIONS.incrementAndGet();
        OPENED_CONNECTIONS.incrementAndGet();

        FullHttpResponse res;

        //IP address of client
        SocketAddress address = ctx.channel().remoteAddress();
        String strAddress = address.toString();
        inc(IP_COUNT, strAddress.substring(1, strAddress.lastIndexOf(':')));

        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
            sendHttpResponse(ctx, req, res);
        } else {
            String url = req.getUri();
            // Allow only GET methods.
            if (req.getMethod() == GET && url != null) {
                if ("/hello".equals(url)) {
                    res = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT.duplicate());
                    res.headers().set(CONTENT_TYPE, "text/plain");
                    res.headers().set(CONTENT_LENGTH, res.content().readableBytes());
                    Thread.sleep(10000);
                    sendHttpResponse(ctx, req, res);
                } else if (url.startsWith("/redirect")) {
                    QueryStringDecoder dec = new QueryStringDecoder(req.getUri());
                    String redirectUrl = dec.parameters().get("url").get(0);
                    res = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
                    sendRedirect(ctx, redirectUrl, res);
                    inc(REDIRECTS, redirectUrl);
                } else if ("/status".equals(url)) {
                    ByteBuf content = HttpServerStatusPage.getContent();
                    res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
                    res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                    setContentLength(res, content.readableBytes());
                    sendHttpResponse(ctx, req, res);
                } else {
                    res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
                    sendHttpResponse(ctx, req, res);
                }
            } else {
                res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
                sendHttpResponse(ctx, req, res);
            }
        }

        //Connection con = new Connection(strAddress, req.getUri(), new Timestamp(new Date().getTime()),res.content().readableBytes(),req.content().readableBytes());
        //CONNECTIONS.add(con);
        setConnections(strAddress, req.getUri(), new Timestamp(new Date().getTime()),res.content().readableBytes(),req.content().readableBytes());
        OPENED_CONNECTIONS.decrementAndGet();
    }

    private static synchronized void setConnections(String ip, String uri, Timestamp time, Integer sent, Integer received) {
        Connection con = new Connection(ip, uri, time, sent, received);
        CONNECTIONS.add(con);
    }

    private static synchronized void inc(Map<String, AtomicInteger> map, String key) {
        AtomicInteger count = map.get(key);
        if (count == null)
            map.put(key, new AtomicInteger(1));
        else
            count.incrementAndGet();
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri, FullHttpResponse res) {
        res.headers().set(LOCATION, newUri);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
    }

    public static AtomicInteger getTotalConnections() {
        return TOTAL_CONNECTIONS;
    }

    public static AtomicInteger getOpenedConnections() {
        return OPENED_CONNECTIONS;
    }

    public static Map<String, AtomicInteger> getIpCount() {
        return IP_COUNT;
    }

    public static Map<String, AtomicInteger> getRedirects() {
        return REDIRECTS;
    }

    public static List<Connection> getConnections() {
        return CONNECTIONS;
    }
}
