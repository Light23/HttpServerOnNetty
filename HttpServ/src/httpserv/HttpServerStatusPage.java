package httpserv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class HttpServerStatusPage {

    private static final String NEWLINE = "\r\n";

    public static ByteBuf getContent() {
        return Unpooled.copiedBuffer(
                        "<h1> Status Page </h1>" + NEWLINE +
                        "<p>General requests count: " + HttpServerHandler.getTotalConnections() + NEWLINE +
                        "<p>Unique requests count: " + HttpServerHandler.getIpCount().size() + NEWLINE +
                        "<p>Requests per IP:" + NEWLINE +
                        //table IP-count
                        "<p><table border='1'><tr><th>IP</th><th>COUNT</th></tr>" + NEWLINE +
                        "" + ipcountView(HttpServerHandler.getIpCount()) +
                        "</table>" + NEWLINE +
                        "<p>Redirects count: " + NEWLINE +
                        //table URL-count
                        "<p><table border='1'><tr><th>URL</th><th>COUNT</th></tr>" + NEWLINE +
                        "" + redirectsView(HttpServerHandler.getRedirects()) +
                        "</table>" + NEWLINE +
                        "<p>Current connections: " + HttpServerHandler.getOpenedConnections() + NEWLINE +
                        "<p>16 last connections: " + NEWLINE +
                        //table src_ip-uri-timestamp-sent_bytes-received_bytes
                        "<p><table border='1'><tr><th>SRC_IP</th><th>URI</th><th>TIMESTAMP</th><th>SENT_BYTES</th><th>RECEIVED_BYTES</th></tr>" + NEWLINE +
                        "" + connectionsView(HttpServerHandler.getConnections()) + NEWLINE +
                        "</table>" + NEWLINE, CharsetUtil.US_ASCII);
    }

    private HttpServerStatusPage() {
        // Unused
    }

    private static String ipcountView(Map<String, AtomicInteger> ipcount) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, AtomicInteger> entry : ipcount.entrySet()) {
            sb.append("<tr><td>");
            sb.append(entry.getKey());
            sb.append("</td><td>");
            sb.append(entry.getValue());
            sb.append("</td></tr>");
        }
        return sb.toString();
    }

    private static String redirectsView(Map<String, AtomicInteger> redirects) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, AtomicInteger> entry : redirects.entrySet()) {
            sb.append("<tr><td>");
            sb.append(entry.getKey());
            sb.append("</td><td>");
            sb.append(entry.getValue());
            sb.append("</td></tr>");
        }
        return sb.toString();
    }

    private static String connectionsView(List<Connection> connections) {
        StringBuilder sb = new StringBuilder();
        if (connections.size() >= 16) {
            for(int i = connections.size()-16; i < connections.size(); i++) {
                sb.append("<tr><td>");
                sb.append(connections.get(i).getSrc_ip().substring(1, connections.get(i).getSrc_ip().lastIndexOf(':')));
                sb.append("</td><td>");
                sb.append(connections.get(i).getUri());
                sb.append("</td><td>");
                sb.append(connections.get(i).getTimestamp());
                sb.append("</td><td>");
                sb.append(connections.get(i).getSent_bytes());
                sb.append("</td><td>");
                sb.append(connections.get(i).getRecived_bytes());
                sb.append("</td></tr>");
            }
        }
        else {
            for(int i = 0; i < connections.size(); i++) {
                sb.append("<tr><td>");
                sb.append(connections.get(i).getSrc_ip().substring(1, connections.get(i).getSrc_ip().lastIndexOf(':')));
                sb.append("</td><td>");
                sb.append(connections.get(i).getUri());
                sb.append("</td><td>");
                sb.append(connections.get(i).getTimestamp());
                sb.append("</td><td>");
                sb.append(connections.get(i).getSent_bytes());
                sb.append("</td><td>");
                sb.append(connections.get(i).getRecived_bytes());
                sb.append("</td></tr>");}
        }
        return sb.toString();
    }
}