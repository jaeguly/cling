package org.oflab.cling.mediaserver.android;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.protocol.HttpRequestHandler;
import org.oflab.cling.mediaserver.android.transport.HttpServer;

import java.io.IOException;
import java.util.logging.Logger;


public class HttpServerService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        // The service is being created
        httpServer = new HttpServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()

        if (!httpServer.isAlive()) {
            try {
                httpServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return binder;
    }

    @Override
    public void onDestroy() {

        // The service is no longer used and is being destroyed
        httpServer.stop();
        httpServer = null;
    }


    public class LocalBinder extends Binder {
        HttpServerService getService() {
            return HttpServerService.this;
        }
    }

    public void addHandler(String pattern, HttpRequestHandler handler) {
        if (httpServer != null) {
            httpServer.getRequestHandlerRegistry().register(pattern, handler);
            Log.e("HttpServerService", "addHandler(request,) returns ok!");
        } else {
            Log.e("HttpServerService", "addHandler(request,) returns fail!");
        }
    }

    public void removeHandler(String pattern) {
        if (httpServer != null) {
            httpServer.getRequestHandlerRegistry().unregister(pattern);
            Log.e("HttpServerService", "removeHandler(request,) returns ok!");
        } else {
            Log.e("HttpServerService", "removeHandler(request,) returns fail!");
        }
    }

    public int getListenPort() {
        if (httpServer != null) {
            return httpServer.getListenPort();
        }

        return -1;
    }

    private static final Logger logger = Logger.getLogger(HttpServerService.class.getName());
    private final IBinder binder = new LocalBinder();
    private HttpServer httpServer;
}
