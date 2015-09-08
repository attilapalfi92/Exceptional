package com.attilapalfi.exceptional.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Created by Attila on 2015-06-20.
 */
public class GpsService extends Service implements LocationListener {
    private static GpsService instance;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 100 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 10; // 10 minute

    private Context context;
    private LocationManager locationManager;
    private Location location; // location
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;

    public static GpsService getInstance( ) {
        if ( instance == null ) {
            instance = new GpsService();
        }

        return instance;
    }

    private GpsService( ) {
    }

    public void initialize( Context context ) {
        this.context = context;
        getLocation();
    }

    public Location getLocation( ) {
        try {
            getNetworkStatus();
            if ( isNetworkEnabled ) {
                canGetLocation = true;
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
        isNetworkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
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
        // getting network status
        isNetworkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
        canGetLocation = isNetworkEnabled;
        if ( !canGetLocation ) {
            Toast.makeText( context, "Can't get device's location.\nPlease enable location services.", Toast.LENGTH_SHORT ).show();
        }

        return canGetLocation;
    }


    public void showSettingsAlert( ) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( context );

        // On pressing Settings button
        alertDialog.setPositiveButton( "Settings", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                Intent intent = new Intent( Settings.ACTION_NETWORK_OPERATOR_SETTINGS );
                context.startActivity( intent );
            }
        } );

        // on pressing cancel button
        alertDialog.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
                dialog.cancel();
            }
        } );

        // Showing Alert Message
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