package com.attilapalf.exceptional.ui.main;


import android.content.Intent;
import android.location.Location;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.exception.*;
import com.attilapalf.exceptional.exception.Exception;
import com.attilapalf.exceptional.ui.LoginActivity;
import com.attilapalf.exceptional.utils.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CopyOnWriteArrayList;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private CopyOnWriteArrayList<com.attilapalf.exceptional.exception.Exception> setLocationExceptions
            = new CopyOnWriteArrayList<>();
    private ExceptionPreferences exceptionPreferences;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        exceptionPreferences = ExceptionPreferences.getInstance(getApplicationContext());

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(!LoginManager.isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
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

    private static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }


    public void throwMeExcClicked(View view) {
        Exception e = ExceptionFactory.createRandomException(exceptionPreferences);

        synchronized (this) {
            if (mLocation == null) {
                setLocationExceptions.add(e);

            } else {
                e.setLocation(mLocation);
                exceptionPreferences.addException(e);
            }
        }


        String data =
                    "Description: " + e.getDescription() + "\n\n" +
                    "From: " + e.getFromWho() + "\n\n" +
                    "Where: " + e.getLocation();

        new MaterialDialog.Builder(this)
                .title(e.getPrefix() + "\n" + e.getShortName())
                .content(data)
                .positiveText("LOL")
                .negativeText("OK")
                .show();
    }


    @Override
    public void onConnected(Bundle bundle) {
        synchronized (this) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            for(Exception e : setLocationExceptions) {
                e.setLocation(mLocation);
                exceptionPreferences.addException(e);
            }

            setLocationExceptions.clear();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Couldn't get device's location.", Toast.LENGTH_SHORT).show();
    }
}
