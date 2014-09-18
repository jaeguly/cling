package org.oflab.cling.mediaserver.android.transport;

import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class HttpServer {

    /**
     * constructor
     */
    public HttpServer() {
        this(0);
    }

    /**
     * contstructor with a port for server socket
     */
    public HttpServer(int port) {
        this.port = port;

        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        registry = new HttpRequestHandlerRegistry();

        BasicHttpProcessor processor = new BasicHttpProcessor();
        processor.addResponseInterceptor(new ResponseDate());
        processor.addResponseInterceptor(new ResponseServer());
        processor.addResponseInterceptor(new ResponseContent());
        processor.addResponseInterceptor(new ResponseConnControl());

        // Set up the HTTP service
        httpService = new HttpService(processor, new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory());

        httpService.setHandlerResolver(registry);
        httpService.setParams(params);
    }

    synchronized public boolean start() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(port));

        listenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                logger.info("listening start..");

                do {
                    try {
                        Socket client = serverSocket.accept();

                        DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                        conn.bind(client, params);

                        Thread worker = new WorkerThread(httpService, conn);
                        worker.setDaemon(true);
                        worker.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (!serverSocket.isClosed());

                logger.info("listening stop..");
            }
        });

        // sets the name of this thread
        listenerThread.setName("HttpServer(" + port + ")");
        listenerThread.setDaemon(true);
        listenerThread.start();

        return true;
    }

    synchronized public void stop() {
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getListenPort() {
        if (serverSocket != null)
            return serverSocket.getLocalPort();

        return 0;
    }

    public boolean isAlive() {
        return (serverSocket != null) && !serverSocket.isClosed()
                && (listenerThread != null) && listenerThread.isAlive();
    }

    class WorkerThread extends Thread {
        HttpService httpService;
        HttpServerConnection httpConn;

        public WorkerThread(HttpService httpService, HttpServerConnection conn) {
            super();
            this.httpService = httpService;
            this.httpConn = conn;
        }

        public void run() {
            HttpContext context = new BasicHttpContext(null);

            try {

                while (!Thread.interrupted()) {
                    if (httpConn.isOpen()) {
                        httpService.handleRequest(httpConn, context);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    httpConn.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public HttpRequestHandlerRegistry getRequestHandlerRegistry() {
        return registry;
    }

    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
    protected HttpRequestHandlerRegistry registry;
    protected HttpParams params;
    protected int port;
    protected Thread listenerThread;
    private ServerSocket serverSocket;
    private HttpService httpService;
}
