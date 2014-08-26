package org.oflab.cling.mediaserver.android;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.oflab.cling.mediaserver.android.transport.HttpServer;

import java.util.logging.Logger;

public class HttpServerService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        // The service is being created
        httpServer = new HttpServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.fine("Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return binder;
    }

    @Override
    public void onDestroy() {

        // The service is no longer used and is being destroyed
        httpServer.stop();
    }


    public class LocalBinder extends Binder {
        HttpServerService getService() {
            return HttpServerService.this;
        }
    }

    private static final Logger logger = Logger.getLogger(HttpServerService.class.getName());
    private final IBinder binder = new LocalBinder();
    private HttpServer httpServer;
}
