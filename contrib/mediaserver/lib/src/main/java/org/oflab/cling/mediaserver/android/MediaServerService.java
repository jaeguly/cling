package org.oflab.cling.mediaserver.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.registry.Registry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class MediaServerService extends Service {

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        MediaServerService getService() {
            return MediaServerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The service is being created
        MediaServer mediaServer = new MediaServer(getApplicationContext());

        upnpServiceConnection = new UpnpServiceConnection(mediaServer);
        getApplicationContext().bindService(
                new Intent(MediaServerService.this, AndroidUpnpServiceImpl.class),
                upnpServiceConnection, Context.BIND_AUTO_CREATE);

        contentServiceConnection = new ContentServiceConnection(mediaServer);
        getApplicationContext().bindService(
                new Intent(MediaServerService.this, HttpServerService.class),
                contentServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // The service is no longer used and is being destroyed
        if (contentServiceConnection != null) {
            contentServiceConnection.abort();
            getApplicationContext().unbindService(contentServiceConnection);
        }

        if (upnpServiceConnection != null) {
            upnpServiceConnection.abort();
            getApplicationContext().unbindService(upnpServiceConnection);
        }
    }

    private InetAddress getInetAddress() {

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

    // monitoring the state of an upnp service.
    private class UpnpServiceConnection implements ServiceConnection {

        UpnpServiceConnection(MediaServer mediaServer) {
            this.mediaServer = mediaServer;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            Registry registry = upnpService.getRegistry();

            LocalDevice mediaServerDevice = registry.getLocalDevice(mediaServer.getUdn(), true);

            if (mediaServerDevice == null) {
                try {

                    mediaServerDevice = mediaServer.createDevice();
                    registry.addDevice(mediaServerDevice);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            upnpService = null;
        }

        public void abort() {
            if (upnpService != null)
                upnpService.getRegistry().removeDevice(mediaServer.getUdn());
        }

        protected AndroidUpnpService upnpService;
        protected MediaServer mediaServer;
    }

    // monitoring the state of a http server delivering local contents
    private class ContentServiceConnection implements ServiceConnection {

        ContentServiceConnection(MediaServer mediaServer) {
            this.mediaServer = mediaServer;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            httpServerService = ((HttpServerService.LocalBinder) service).getService();
            httpServerService.addHandler("*", mediaServer);

            int port = httpServerService.getListenPort();

            mediaServer.loadContainers("http://" + getInetAddress().getHostAddress() + ":" + port);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            httpServerService = null;
        }

        public void abort() {
            if (httpServerService != null)
                httpServerService.removeHandler("*");
        }

        protected HttpServerService httpServerService;
        protected MediaServer mediaServer;
    }

    private static final Logger logger = Logger.getLogger(MediaServerService.class.getName());
    private final IBinder binder = new LocalBinder();
    protected UpnpServiceConnection upnpServiceConnection;
    protected ContentServiceConnection contentServiceConnection;
}
