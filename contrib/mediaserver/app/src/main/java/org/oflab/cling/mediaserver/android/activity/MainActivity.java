package org.oflab.cling.mediaserver.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.oflab.cling.mediaserver.android.MediaServerService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // rest a root log handler for cling
        org.seamless.util.logging.LoggingUtil.resetRootHandler(
                new org.oflab.cling.mediaserver.android.util.FixedAndroidLogHandler()
        );

        startService(new Intent(this, MediaServerService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, MediaServerService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.add(0, 0, 0, R.string.switchRouter).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 1, 0, R.string.toggleDebugLogging).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, 2, 0, R.string.appExit);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
//            case 0:
//                if (upnpService != null) {
//                    Router router = upnpService.get().getRouter();
//
//                    try {
//                        if (router.isEnabled()) {
//                            Toast.makeText(this, R.string.disablingRouter, Toast.LENGTH_SHORT).show();
//                            router.disable();
//                        } else {
//                            Toast.makeText(this, R.string.enablingRouter, Toast.LENGTH_SHORT).show();
//                            router.enable();
//                        }
//                    } catch (RouterException ex) {
//                        CharSequence text = getText(R.string.errorSwitchingRouter) + ex.toString();
//                        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//                        ex.printStackTrace(System.err);
//                    }
//                }
//                return true;

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

            case 2:
                Toast.makeText(this, R.string.exitingService, Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }

        Toast.makeText(this, R.string.notYet, Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

}
