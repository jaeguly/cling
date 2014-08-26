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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class HttpServer {

    public HttpServer() {
        this(0);
    }

    public HttpServer(int port) {
        this.port = port;

        params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        registry = new HttpRequestHandlerRegistry();
    }

    synchronized public boolean start() {
        InetAddress inetAddress = getInetAddress();

        if (inetAddress == null) {
            logger.info("can't start server.");
            return false;
        }

        try {
            listenerThread = new ListenerThread(inetAddress, port, params, registry);
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    synchronized  public void stop() {
        if (listenerThread != null) {
            listenerThread.abort();
        }
    }

    public InetAddress getInetAddress() {
        InetAddress foundInetAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        foundInetAddress = inetAddress;
                        break;
                    }
                }
            }
        } catch (SocketException se) {
            logger.warning("error: retrieving network interfaces");
        }

        return foundInetAddress;
    }

    class ListenerThread extends Thread {
        private ServerSocket serverSocket;
        private HttpParams params;
        private HttpService httpService;
        private boolean stopped;

        public ListenerThread(InetAddress address, int port, HttpParams params,
                              HttpRequestHandlerRegistry handlerRegistry) throws IOException {
            this.serverSocket = new ServerSocket(port, 0, address);
            this.params = params;

            BasicHttpProcessor httpProc = new BasicHttpProcessor();
            httpProc.addResponseInterceptor(new ResponseDate());
            httpProc.addResponseInterceptor(new ResponseServer());
            httpProc.addResponseInterceptor(new ResponseContent());
            httpProc.addResponseInterceptor(new ResponseConnControl());

            // Set up the HTTP service
            httpService = new HttpService(httpProc,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory());

            httpService.setHandlerResolver(handlerRegistry);
            httpService.setParams(params);

            stopped = false;

            // sets the name of this thread
            setName("HttpServer(" + port + ")");
        }

        @Override
        public void run() {
            logger.info("listening start..");

            while (!stopped) {
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
            }

            logger.info("listening stop..");
        }

        public void abort() {
            try {
                stopped = true;

                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
    protected HttpRequestHandlerRegistry registry;
    protected HttpParams params;
    protected int port;
    protected ListenerThread listenerThread;
}
