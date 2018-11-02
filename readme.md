# ����async-http-client�ĸ������첽����http���������
async-http-client�ǻ���netty���첽http�����ܣ�����netty���첽�ص�����ͬʱ���Ͷ��http����
���ǣ�async-http-client��ʹ�ù����л�����һϵ�е����⣬���磺
1��async-http-clientʹ���첽�ص��ķ�ʽ����response������ĳЩҵ�񳡾��£���Ҫ�����response�ۼ���һ�����Ծ���Ҫ�߳�ͬ���Ļ��ơ�
2��async-http-client�����������Ϊ����ԭ��ʧ�ܣ����������ȶ���Ҫ��ߵ�ҵ�񳡾���Ҫ������Щ�쳣��
3��async-http-client����http�����л�ռ�ÿͻ��˶˿ڣ����������Ҫ�����ܹ�ͬʱ��������ĸ�����
������Щԭ���ҷ�װ��һ�������������ܸ�Ч�ȶ��Ĺ����� CompletableAsyncHttpManager��
������������߳�֮�������ͬ�����̵߳ȴ����Լ����ݵĴ����ش����Ͳ�����Ŀ�Ŀ��ơ�
## ʹ������
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
���ȹ���һ��async-http-client��client��Ȼ����CompletableAsyncHttpManager��������һ�η�װ��Ȼ����ô������url��Ϳ���ͨ��getRequestBatch
�������ݵ������ˡ��ڷ��صĽ���л�������ɹ������ݺ�����ʧ�ܵ����ݡ�һ��������������������������ݶ���ɹ������Ҳ��������ݶ�ʧ��������һЩ
��������£�CompletableAsyncHttpManager�ڳ����ش���κ���Ȼʧ�ܣ���ʱ�Ὣʧ�ܵ�url�Ͷ�Ӧ���쳣��Ϣͨ��result���ػ��������û�����

## Benchmark
![image](https://raw.githubusercontent.com/Yves-yuan/batch-async-http-request/master/img/benchmark1.png)
������һ�����������http�����������������ѵĺ���ʱ�䣬���Կ���ʹ�� CompletableAsyncHttpManager ���Ժ����׵ؾʹﵽÿ�뼸ǧ��
http�����û�������ͨ������setMaxConnections�����http����Ĳ����Ȼ�ȡ���õ����ܡ�