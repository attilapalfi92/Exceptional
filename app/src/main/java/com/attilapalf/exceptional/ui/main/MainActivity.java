package com.attilapalf.exceptional.ui.main;


import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.attilapalf.exceptional.MyApplication;
import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.interfaces.ServerResponseListener;
import com.attilapalf.exceptional.ui.LoginActivity;
import com.attilapalf.exceptional.ui.OptionsActivity;
import com.attilapalf.exceptional.ui.exceptionSending.SendExceptionListActivity;
import com.attilapalf.exceptional.services.ExceptionFactory;
import com.attilapalf.exceptional.services.FacebookManager;
import com.attilapalf.exceptional.services.GpsService;

public class MainActivity extends AppCompatActivity implements ServerResponseListener {

    private Location mLocation;
    private String androidId;
    private MainPagerAdapter adapter;

    GpsService gpsService;

    // TODO: Check for Google Play Services APK
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GpsService.getInstance().initialize(getApplicationContext());
        gpsService = GpsService.getInstance();

        BackendConnector.getInstance().addConnectionListener(this);

        androidId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        adapter = new MainPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int startPage = bundle.getInt("startPage");
            if (startPage != 0) {
                pager.setCurrentItem(startPage);
            }
        }
    }


    // TODO: Check for Google Play Services APK
    @Override
    protected void onResume() {
        super.onResume();

        if(!FacebookManager.getInstance().isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackendConnector.getInstance().removeConnectionListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, OptionsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private int backButtonCount = 0;

    /**
     * Back button listener.
     * Will close the application if the back button pressed twice.
     */
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void throwExceptionClicked(View view) {
        if (MyApplication.isLoggedIn()) {
            Intent intent = new Intent(this, SendExceptionListActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "You have to login first!", Toast.LENGTH_SHORT).show();
        }
    }


    public void giveMeExcClicked(View view) {
        if (MyApplication.isLoggedIn()) {
            if (!gpsService.canGetLocation() && mLocation == null) {
                //Toast.makeText(this, "Can't get device's location.\nPlease enable location services.", Toast.LENGTH_SHORT).show();

            } else {

                mLocation = gpsService.getLocation();
                // TODO: giving exception to myself: not setting the instance ID, and not saving the exception here.
                // TODO: save the exception when in arrives from the backend
                Exception e = ExceptionFactory.createRandomException(FacebookManager.getInstance().getProfileId(),
                        FacebookManager.getInstance().getProfileId());

                e.setLongitude(mLocation.getLongitude());
                e.setLatitude(mLocation.getLatitude());
                //ExceptionManager.addException(e);

//            for (ExceptionChangeListener listener : exceptionChangeListeners) {
//                listener.onExceptionsChanged();
//            }

                BackendConnector.getInstance().sendException(e);

                String data =
                        "Description: " + e.getDescription() + "\n\n" +
                                "From: " + e.getFromWho() + "\n\n" +
                                "Where: " + e.getLongitude() + ", " +
                                e.getLatitude();

                new MaterialDialog.Builder(this)
                        .title(e.getPrefix() + "\n" + e.getShortName())
                        .content(data)
                        .positiveText("LOL")
                        .negativeText("OK")
                        .show();
            }


        } else {
            Toast.makeText(this, "You have to login first!", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onConnectionFailed(String what, String why) {
        Toast.makeText(this, what + why, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
