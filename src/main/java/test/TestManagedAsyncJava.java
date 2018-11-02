package test;

import http.AsyncBatchHttpResult;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import http.CompletableAsyncHttpManager;
import java.util.Date;
import java.util.Vector;

public class TestManagedAsyncJava {
    public static void main(String[] args) {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(2000)
                .setMaxConnections(1000);
        DefaultAsyncHttpClient s = new DefaultAsyncHttpClient(clientBuilder.build());
        CompletableAsyncHttpManager m = new CompletableAsyncHttpManager(s, 1);
        System.out.println("开始时间:" + new Date());
        Vector<String> urls = new Vector<>();
        for (int i = 0; i < 1000; i++) {
//            CompletableFuture<Response> c = m.SendRequest("http://10.217.2.226:28080/city?longi=116.481488&latit=39.990464", (r) -> {
            urls.add("http://localhost:8001/test");
        }
        AsyncBatchHttpResult result = null;
        try {
            result = m.getRequestBatch(urls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert result != null;
        System.out.println(result.getData().size());
        System.out.println(result.getErrors().size());
        try {
            System.out.println("关闭开始");
            m.close();
            System.out.println("关闭完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
