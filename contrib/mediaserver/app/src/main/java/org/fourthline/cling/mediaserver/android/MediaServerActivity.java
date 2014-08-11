package org.fourthline.cling.mediaserver.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationError;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MediaServerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // rest a root log handler for cling
        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new org.fourthline.cling.android.FixedAndroidLogHandler()
        );

        // Connect to an application service, creating it if needed.
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().permitAll().build()
        );
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        // Disconnect from an application service.
        getApplicationContext().unbindService(serviceConnection);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.add(0, 0, 0, R.string.switchRouter).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, R.string.toggleDebugLogging).setIcon(android.R.drawable.ic_menu_info_details);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case 0:
                if (upnpService != null) {
                    Router router = upnpService.get().getRouter();

                    try {
                        if (router.isEnabled()) {
                            Toast.makeText(this, R.string.disablingRouter, Toast.LENGTH_SHORT).show();
                            router.disable();
                        } else {
                            Toast.makeText(this, R.string.enablingRouter, Toast.LENGTH_SHORT).show();
                            router.enable();
                        }
                    } catch (RouterException ex) {
                        CharSequence text = getText(R.string.errorSwitchingRouter) + ex.toString();
                        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                        ex.printStackTrace(System.err);
                    }
                }

                return true;

            case 1:
                Logger logger = Logger.getLogger("org.fourthline.cling");

                if (logger.getLevel() != null && !logger.getLevel().equals(Level.INFO)) {
                    Toast.makeText(this, R.string.disablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, R.string.enablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {

            LocalDevice device = getLocalDevice();

            if (device == null) {
                AndroidUpnpService upnpService = (AndroidUpnpService) binder;

                try {
                    Toast.makeText(MediaServerActivity.this, R.string.registeringDevice, Toast.LENGTH_SHORT).show();

                    upnpService.getRegistry().addDevice(createLocalDevice());
                } catch (RegistrationException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                } catch (LocalServiceBindingException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                } catch (ValidationException validationException) {
                    for (ValidationError ve : validationException.getErrors()) {
                        Log.e(TAG, ve.getMessage());
                    }

                    validationException.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }

    };


    protected LocalDevice getLocalDevice() {
        if (upnpService == null)
            return null;

        return upnpService.getRegistry().getLocalDevice(udn, true);
    }


    private LocalDevice createLocalDevice() throws ValidationException, LocalServiceBindingException, IOException {
        // Unique device name
        udn = new UDN(UUID.randomUUID()); // TODO: Not stable!
        // TEST: got from UUID.randomUUID()
        udn = new UDN("571c121c-c10a-428c-8ec1-62d3676c105d");

        // Unique device name, received and offered during discovery with SSDP.
        DeviceIdentity identity = new DeviceIdentity(udn);

        // i.e.   urn:my-domain-namespace:device:MyDevice:1
        DeviceType type = new UDADeviceType("MediaServer", 1);
        DeviceDetails details = new DeviceDetails("Nexus7 (2012)",
                new ManufacturerDetails("4thline"),
                new ModelDetails("ClingMediaServer", "basic media server", "v1")
        );

        AnnotationLocalServiceBinder binder = new AnnotationLocalServiceBinder();

        LocalService[] myLocalServices = new LocalService[]{
                createContentDirectoryService(binder)
        };

        return new LocalDevice(identity, type, details, createDefaultDeviceIcon(), myLocalServices);
    }

    // ContentDirectory
    private LocalService<AndroidContentDirectoryService> createContentDirectoryService(
            AnnotationLocalServiceBinder binder) {

        LocalService<AndroidContentDirectoryService> contentDirectoryService
                = binder.read(AndroidContentDirectoryService.class);

        contentDirectoryService.setManager(new DefaultServiceManager<AndroidContentDirectoryService>(
                contentDirectoryService, AndroidContentDirectoryService.class));

        return contentDirectoryService;
    }


    protected Icon createDefaultDeviceIcon() {
        return null;
    }

    private static final String TAG = MediaServerActivity.class.getSimpleName();
    private AndroidUpnpService upnpService;
    private UDN udn;

}
