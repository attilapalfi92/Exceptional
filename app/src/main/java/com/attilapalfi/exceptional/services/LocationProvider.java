package com.attilapalfi.exceptional.services;

import javax.inject.Inject;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Attila on 2015-06-20.
 */
public class LocationProvider implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000 * 10;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    @Inject
    Context context;
    private Location location;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean updatingLocation = false;


    public LocationProvider( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        connectToGoogleApiClient();
        createLocationRequest();
        startLocationUpdates();
    }

    public void startLocationUpdates( ) {
        if ( !updatingLocation && googleApiClient.isConnected() ) {
            updatingLocation = true;
            LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, locationRequest, this );
        }
    }

    private void connectToGoogleApiClient( ) {
        googleApiClient = new GoogleApiClient.Builder( context )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();
        googleApiClient.connect();
    }

    private void createLocationRequest( ) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval( UPDATE_INTERVAL_IN_MILLISECONDS );
        locationRequest.setFastestInterval( FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS );
        locationRequest.setPriority( LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY );
    }

    public Location getLocation( ) throws LocationException {
        if ( location == null ) {
            location = LocationServices.FusedLocationApi.getLastLocation( googleApiClient );
        }
        handleNullLocation();
        return location;
    }

    private void handleNullLocation( ) throws LocationException {
        if ( location == null ) {
            Toast.makeText( context, R.string.locaton_service_enabled_but_location_was_null, Toast.LENGTH_LONG ).show();
            throw new LocationException( context.getString( R.string.locaton_service_enabled_but_location_was_null ) );
        }
    }

    public void stopLocationUpdates( ) {
        if ( updatingLocation ) {
            updatingLocation = false;
            LocationServices.FusedLocationApi.removeLocationUpdates( googleApiClient, this );
        }
    }

    @Override
    public void onConnected( Bundle bundle ) {
        startLocationUpdates();
        Location location = LocationServices.FusedLocationApi.getLastLocation( googleApiClient );
        if ( location != null ) {
            this.location = location;
        }
    }

    @Override
    public void onConnectionSuspended( int i ) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult ) {
        Toast.makeText( context, "Connecting to Google Play Services failed.", Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onLocationChanged( Location location ) {
        this.location = location;
    }
}