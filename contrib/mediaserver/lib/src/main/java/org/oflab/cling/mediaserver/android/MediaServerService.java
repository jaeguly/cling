package org.oflab.cling.mediaserver.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

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
    }

    private class ContentHttpServerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private static final Logger logger = Logger.getLogger(MediaServerService.class.getName());
    private final IBinder binder = new LocalBinder();
}
