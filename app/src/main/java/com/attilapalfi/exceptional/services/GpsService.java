package com.attilapalfi.exceptional.services;

import javax.inject.Inject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
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
    private String currentProvider;
    private boolean networkEnabled = false;
    private boolean gpsEnabled;
    private boolean passiveEnabled;

    public GpsService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        locationManager = (LocationManager) context.getSystemService( LOCATION_SERVICE );
        getNetworkStatus();
    }

    public Location getLocation( ) {
        try {
            if ( canGetLocation() ) {
                initCurrentProvider();
                getLastKnownLocation();
                stopUsingGps();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return location;
    }

    public boolean canGetLocation( ) {
        getNetworkStatus();
        boolean isAvailable = passiveEnabled || networkEnabled || gpsEnabled;
        if ( !isAvailable ) {
            Toast.makeText( context, R.string.cant_get_location_pls_enable, Toast.LENGTH_LONG ).show();
        }
        return isAvailable;
    }

    private void getNetworkStatus( ) {
        passiveEnabled = locationManager.isProviderEnabled( LocationManager.PASSIVE_PROVIDER );
        networkEnabled = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
        gpsEnabled = locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
    }

    private void initCurrentProvider( ) {
        if ( passiveEnabled ) {
            currentProvider = LocationManager.PASSIVE_PROVIDER;
        } else if ( networkEnabled ) {
            currentProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            currentProvider = LocationManager.GPS_PROVIDER;
        }
        initLocationProvider();
    }

    private void initLocationProvider() {
        locationManager.requestLocationUpdates(
                currentProvider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
    }

    private void getLastKnownLocation( ) {
        if ( locationManager != null ) {
            location = locationManager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
        }
    }

    public void stopUsingGps( ) {
        if ( locationManager != null ) {
            locationManager.removeUpdates( this );
        }
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