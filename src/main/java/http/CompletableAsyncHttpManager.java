package http;

import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.asynchttpclient.netty.NettyResponseFuture;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;


public class CompletableAsyncHttpManager {
    private DefaultAsyncHttpClient client;
    private NioEventLoopGroup p;
    private ReentrantLock l = new ReentrantLock(true);
    private Thread t = null;
    private final Object lock = new Object();
    private int maxRetry = 10;
    private int maxWaitMilliseconds = 10000;

    public CompletableAsyncHttpManager(DefaultAsyncHttpClient s, int callBackThreadCount) {
        client = s;
        p = new NioEventLoopGroup(callBackThreadCount);
    }

    public CompletableAsyncHttpManager(DefaultAsyncHttpClient s, int callBackThreadCount, int maxRetry) {
        client = s;
        p = new NioEventLoopGroup(callBackThreadCount);
        this.maxRetry = maxRetry;
    }

    public int getMaxWaitMilliseconds() {
        return maxWaitMilliseconds;
    }

    public void setMaxWaitMilliseconds(int maxWaitMilliseconds) {
        this.maxWaitMilliseconds = maxWaitMilliseconds;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public AsyncBatchHttpResult getRequestBatch(Vector<String> urls) throws Exception {
        final AtomicInteger atom = new AtomicInteger(0);
        ArrayList<CompletableFuture<Response>> cf = new ArrayList<>();
        final Vector<ErrorRequest> errors = new Vector<>();
        final Vector<ErrorRequest> errorToTries = new Vector<>();
        final Vector<String> data = new Vector<>();
        final int urlSize = urls.size();
        for (String url : urls) {
            CompletableFuture<Response> c = SendRequest(url, (r) -> {
                synchronized (lock) {
                    atom.incrementAndGet();
                    if (atom.get() > urlSize) {
                    }
                    data.add(r.toString());
                    if (atom.get() == urlSize) {
                        lock.notifyAll();
                    }
                }
            }, (future, errorUrl, exc) -> {
                synchronized (lock) {
                    atom.incrementAndGet();
                    errors.add(new ErrorRequest(errorUrl, exc));
                    if (atom.get() == urlSize) {
                        lock.notifyAll();
                    }

                }
            });
            cf.add(c);
        }
        waitSync(atom, urlSize);
        int retryRemain = maxRetry;
        while (!errors.isEmpty() && retryRemain > 0) {
            retryRemain -= 1;
            atom.set(0);
            final int retrySize = errors.size();
            errorToTries.clear();
            errorToTries.addAll(errors);
            errors.clear();
            for (ErrorRequest anErrorsExc : errorToTries) {
                CompletableFuture<Response> c = SendRequest(anErrorsExc.getUrl(), (r) -> {
                    synchronized (lock) {
                        atom.incrementAndGet();
                        data.add(r.toString());
                        if (atom.get() == retrySize) {
                            lock.notifyAll();
                        }
                    }
                }, (future, url, exc) -> {
                    synchronized (lock) {
                        atom.incrementAndGet();
                        errors.add(new ErrorRequest(url, exc));
                        if (atom.get() == retrySize) {
                            lock.notifyAll();
                        }
                    }
                });
                cf.add(c);
            }
            waitSync(atom, retrySize);
        }
        return new AsyncBatchHttpResult(data, errors);
    }

    private void waitSync(AtomicInteger atom, int des) {
        int waitTimes = maxWaitMilliseconds / 500;
        while (waitTimes > 0) {
            synchronized (lock) {
                if (atom.get() != des) {
                    try {
                        waitTimes -= 1;
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    private CompletableFuture<Response> SendRequest(String url, SparkHttpRunnable successCall, SparkHttpOnFailure
            failureCall) throws Exception {
        BoundRequestBuilder getRequest = client.prepareGet(url);
        NettyResponseFuture<Response> responseFutureTmp;
        int maxRetries = 100;
        while (true) {
            l.lock();
            ListenableFuture<Response> ftmp = getRequest.execute();
            if (!(ftmp instanceof NettyResponseFuture)) {
                ListenableFuture.CompletedFailure cf = (ListenableFuture.CompletedFailure) ftmp;
                cf.get();
            }
            NettyResponseFuture<Response> tmpR = (NettyResponseFuture<Response>) ftmp;
            if (tmpR.toCompletableFuture().isCompletedExceptionally()) {
                maxRetries -= 1;
                if (maxRetries > 0) {
                    l.unlock();
                } else {
                    //block
                    t = Thread.currentThread();
                    l.unlock();
                    LockSupport.parkNanos(100000000);
                }
            } else {
                responseFutureTmp = tmpR;
                l.unlock();
                break;
            }
        }
        final NettyResponseFuture<Response> responseFuture = responseFutureTmp;
        Runnable callback = () -> {
            try {
                Response response = responseFuture.get();
                l.lock();
                successCall.run(response);
                LockSupport.unpark(t);
                l.unlock();
            } catch (InterruptedException | ExecutionException e) {
                failureCall.run(responseFuture, url, e);
            }
        };
        responseFuture.addListener(callback, p);
        return responseFuture.toCompletableFuture();
    }

    public void close() {
        client.close();
        p.shutdownGracefully();
    }
}
