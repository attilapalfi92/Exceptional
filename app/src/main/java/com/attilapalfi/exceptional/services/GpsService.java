package com.attilapalfi.exceptional.services;

import javax.inject.Inject;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;
import com.attilapalfi.exceptional.dependency_injection.Injector;

/**
 * Created by Attila on 2015-06-20.
 */
public class GpsService extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 100 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 10; // 10 minute

    @Inject Context context;
    private LocationManager locationManager;
    private Location location; // location
    private boolean networkEnabled = false;

    public GpsService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        getNetworkStatus();
    }

    public Location getLocation( ) {
        try {
            if ( networkEnabled ) {
                initLocationNetworkProvider();
                getLastKnownLocation();
            } else {
                // no network provider is enabled
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return location;
    }

    private void getNetworkStatus( ) {
        locationManager = (LocationManager) context.getSystemService( LOCATION_SERVICE );
        networkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
    }

    // before
    private void initLocationNetworkProvider( ) {
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
    }

    // before
    private void getLastKnownLocation( ) {
        if ( locationManager != null ) {
            location = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
        }
    }

    // before
    public void stopUsingGps( ) {
        if ( locationManager != null ) {
            locationManager.removeUpdates( GpsService.this );
        }
    }

    public boolean canGetLocation( ) {
        networkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
        if ( !networkEnabled ) {
            Toast.makeText( context, "Can't get device's location.\nPlease enable location services.", Toast.LENGTH_SHORT ).show();
        }
        return networkEnabled;
    }


    public void showSettingsAlert( ) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( context );

        alertDialog.setPositiveButton( "Settings", ( dialog, which ) -> {
            Intent intent = new Intent( Settings.ACTION_NETWORK_OPERATOR_SETTINGS );
            context.startActivity( intent );
        } );

        alertDialog.setNegativeButton( "Cancel", ( dialog, which ) -> {
            dialog.cancel();
        } );

        alertDialog.show();
    }


    @Override
    public void onLocationChanged( Location location ) {
    }

    @Override
    public void onProviderDisabled( String provider ) {
    }

    @Override
    public void onProviderEnabled( String provider ) {
    }

    @Override
    public void onStatusChanged( String provider, int status, Bundle extras ) {
    }

    @Override
    public IBinder onBind( Intent arg0 ) {
        return null;
    }
}