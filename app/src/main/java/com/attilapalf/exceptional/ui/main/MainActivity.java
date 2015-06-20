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
import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.rest.ConnectionFailedListener;
import com.attilapalf.exceptional.ui.LoginActivity;
import com.attilapalf.exceptional.utils.ExceptionFactory;
import com.attilapalf.exceptional.utils.ExceptionManager;
import com.attilapalf.exceptional.utils.FacebookManager;
import com.attilapalf.exceptional.utils.GpsTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ExceptionSource, ConnectionFailedListener {

    private Location mLocation;
    private final Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private String androidId;

    GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsTracker = new GpsTracker(getApplicationContext());

        BackendConnector.getInstance().addConnectionListener(this);

        androidId = Settings.Secure.getString(getApplicationContext()
                .getContentResolver(), Settings.Secure.ANDROID_ID);

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(!FacebookManager.isUserLoggedIn()) {
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
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }



    public void throwMeExcClicked(View view) {
        Exception e = ExceptionFactory.createRandomException(FacebookManager.getProfileId(),
                FacebookManager.getProfileId(), ExceptionManager.getNextId());

        if (gpsTracker.canGetLocation()) {
            mLocation = gpsTracker.getLocation();

        }

        if (!gpsTracker.canGetLocation() && mLocation == null) {
            gpsTracker.showSettingsAlert();

        } else {
            e.setLongitude(mLocation.getLongitude());
            e.setLatitude(mLocation.getLatitude());
            ExceptionManager.addException(e);

            for (ExceptionChangeListener listener : exceptionChangeListeners) {
                listener.onExceptionsChanged();
            }

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
    }



    public void asyncTestBtnClicked(View view) {
        FacebookManager.testAsyncCall(exceptionChangeListeners);
    }


    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.add(listener);
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.remove(listener);
    }

    @Override
    public void onConnectionFailed(String what, String why) {
        Toast.makeText(this, what + " " + why, Toast.LENGTH_LONG).show();
    }
}
