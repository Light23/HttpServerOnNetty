package httpserv;

import java.sql.Timestamp;


public class Connection {
    private String src_ip;
    private String uri;
    private Timestamp timestamp;
    private Integer sent_bytes;
    private Integer recived_bytes;

    public Connection() {}

    public Connection(String src_ip, String uri, Timestamp timestamp, Integer sent_bytes, Integer recived_bytes) {
        this.src_ip = src_ip;
        this.uri = uri;
        this.timestamp = timestamp;
        this.sent_bytes = sent_bytes;
        this.recived_bytes = recived_bytes;
    }


    public String getSrc_ip() {
        return src_ip;
    }

    public void setSrc_ip(String src_ip) {
        this.src_ip = src_ip;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getSent_bytes() {
        return sent_bytes;
    }

    public void setSent_bytes(Integer sent_bytes) {
        this.sent_bytes = sent_bytes;
    }

    public Integer getRecived_bytes() {
        return recived_bytes;
    }

    public void setRecived_bytes(Integer recived_bytes) {
        this.recived_bytes = recived_bytes;
    }
}
