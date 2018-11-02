# 基于async-http-client的高性能异步批量http请求管理器
async-http-client是基于netty的异步http请求框架，利用netty的异步回调可以同时发送多个http请求。
但是，async-http-client的使用过程中回遇到一系列的问题，例如：
1、async-http-client使用异步回调的方式处理response，而在某些业务场景下，需要将多个response聚集到一起，所以就需要线程同步的机制。
2、async-http-client的请求可能因为各种原因失败，对于数据稳定性要求高的业务场景需要处理这些异常。
3、async-http-client发送http过程中会占用客户端端口，因此往往需要限制能够同时进行请求的个数。
基于这些原因，我封装了一个简单易用且性能高效稳定的管理器 CompletableAsyncHttpManager。
它会帮助处理线程之间的数据同步和线程等待，以及数据的错误重传，和并发数目的控制。
## 使用例子
`   DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(2000)
                .setMaxConnections(1000);
    DefaultAsyncHttpClient s = new DefaultAsyncHttpClient(clientBuilder.build());
    CompletableAsyncHttpManager m = new CompletableAsyncHttpManager(s, 1);
	Vector<String> urls = new Vector<>();
    for (int i = 0; i < 1000; i++) {
        urls.add("http://localhost:8001/test");
    }
	AsyncBatchHttpResult result = null;
	try {
        result = m.getRequestBatch(urls);
    } catch (Exception e) {
        e.printStackTrace();
    }
	`
首先构造一个async-http-client的client，然后用CompletableAsyncHttpManager对它进行一次封装，然后构造好待请求的url后就可以通过getRequestBatch
进行数据的请求了。在返回的结果中会有请求成功的数据和请求失败的数据。一般在网络正常的情况下所有数据都会成功返回且不会有数据丢失，但是在一些
极端情况下，CompletableAsyncHttpManager在尝试重传多次后仍然失败，此时会将失败的url和对应的异常信息通过result返回回来交给用户管理。

## Benchmark
![image](https://raw.githubusercontent.com/Yves-yuan/batch-async-http-request/master/img/benchmark1.png)
横轴是一次批量请求的http个数，纵轴是所花费的毫秒时间，可以看到使用 CompletableAsyncHttpManager 可以很容易地就达到每秒几千的
http请求。用户还可以通过设置setMaxConnections来提高http请求的并发度获取更好的性能。