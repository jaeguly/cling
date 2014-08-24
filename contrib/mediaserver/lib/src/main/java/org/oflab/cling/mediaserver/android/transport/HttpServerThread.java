package org.oflab.cling.mediaserver.android.transport;

import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * http server based worker-threads
 */
public class HttpServerThread extends Thread {

    /**
     * Constructor
     */
    public HttpServerThread(int port) {
        this.port = port;

        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 0)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        registry = new HttpRequestHandlerRegistry();

        workerThreadInPool = new ArrayList<WorkerThread>();

        // sets the name of this thread
        serverName = "HTTPServerThread(" + port + ")";
        setName(serverName);
    }

    public HttpServerThread(int port, HttpRequestHandler device) {
        this(port);
        setHttpRequestHandler(device);
    }

    public void close() {
        closed = true;

        this.interrupt();

        if (workerThreadPool != null) {
            synchronized (workerThreadInPool) {
                Iterator<WorkerThread> it = workerThreadInPool.iterator();

                while (it.hasNext()) {
                    it.next().close();
                }
            }

            workerThreadPool.shutdown();
            workerThreadPool = null;
        }
        if (httpService != null) {
            httpService.setHandlerResolver(new HttpRequestHandlerRegistry());
            httpService = null;
        }
        registry = null;
    }

    public void setHttpRequestHandler(HttpRequestHandler device) {
        // Set a  new request handlers
        registry.register("*", device);
        setHttpRequestHandler();
    }

    public void registerHandler(String pattern, HttpRequestHandler device) {
        registry.register(pattern, device);
    }

    public void registerHandlers(Map<String, ? extends HttpRequestHandler> map) {
        registry.setHandlers(map);
    }

    public void setHttpRequestHandler() {
        HttpProcessor httpproc = new BasicHttpProcessor();
        ((BasicHttpProcessor) httpproc).addResponseInterceptor(new ResponseDate());
        ((BasicHttpProcessor) httpproc).addResponseInterceptor(new ResponseServer());
        ((BasicHttpProcessor) httpproc).addResponseInterceptor(new ResponseContent());
        ((BasicHttpProcessor) httpproc).addResponseInterceptor(new ResponseConnControl());

        // Set up the HTTP service
        httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory());

        httpService.setHandlerResolver(registry);
        httpService.setParams(params);
    }

    public void execute(Runnable runnable) {

        if (closed) {
            return;
        }

        try {
            ExecutorService es = workerThreadPool;

            if (es != null && !es.isShutdown())
                es.execute(runnable);

            if (runnable instanceof WorkerThread && workerThreadInPool != null) {
                synchronized (workerThreadInPool) {
                    workerThreadInPool.add((WorkerThread) runnable);
                }
            }
        } catch (RejectedExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Runnable createHttpRequestRunnable(final HttpServerConnection conn, final String ipAddr) {
        return new WorkerThread(httpService, conn, ipAddr);
    }


    public class WorkerThread implements Runnable {

        protected final HttpService httpservice;
        protected final HttpServerConnection conn;
        protected final String ip;
        private static final String defaultName = "HTTPWorker";
        public static final String IP_ADDRESS = "IP_ADDRESS";

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
            this(httpservice, conn, null);
        }

        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn,
                            final String ip) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
            this.ip = ip;
            setName(defaultName);
        }

        /**
         * starts executing the worker part of the http server.
         */
        @Override
        public void run() {
            HttpContext context = new BasicHttpContext(null);
            context.setAttribute(IP_ADDRESS, ip);
            try {
                while (this.conn.isOpen()) {
                    ExecutorService es = workerThreadPool;

                    if (es != null && !es.isShutdown())
                        this.httpservice.handleRequest(this.conn, context);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {
                }
            }

            synchronized (workerThreadInPool) {
                workerThreadInPool.remove(this);
            }
        }

        public void close() {
            try {
                this.conn.close();
            } catch (IOException ignore) {
            }
        }
    }

    protected ExecutorService workerThreadPool = Executors.newCachedThreadPool();
    protected HttpRequestHandlerRegistry registry;
    protected final HttpParams params;
    protected HttpService httpService;
    protected int port;
    private final String serverName;
    private List<WorkerThread> workerThreadInPool;
    private boolean closed = false;


}
