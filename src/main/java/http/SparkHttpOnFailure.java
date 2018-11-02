package http;

import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponseFuture;

public interface SparkHttpOnFailure {
    void run(NettyResponseFuture<Response> future, String url, Exception e);
}
