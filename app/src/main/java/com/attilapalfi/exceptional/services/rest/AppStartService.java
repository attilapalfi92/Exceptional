package com.attilapalfi.exceptional.services.rest;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.*;
import com.attilapalfi.exceptional.services.rest.messages.AppStartRequest;
import com.attilapalfi.exceptional.services.rest.messages.AppStartResponse;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java8.util.stream.Collectors;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by palfi on 2015-09-12.
 */
public class AppStartService {
    @Inject Context context;
    @Inject ExceptionInstanceManager exceptionInstanceManager;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject FriendRealm friendRealm;
    @Inject YourselfRealm yourselfRealm;
    @Inject MetadataStore metadataStore;
    @Inject RestInterfaceFactory restInterfaceFactory;
    private String projectNumber;
    private AppStartRestInterface appStartRestInterface;
    private GoogleCloudMessaging googleCloudMessaging;
    private String registrationId;
    private String androidId;
    private AppStartRequest requestBody = new AppStartRequest();

    public void setAndroidId( String aId ) {
        androidId = aId;
    }

    public AppStartService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        projectNumber = context.getString( R.string.project_number );
        appStartRestInterface = restInterfaceFactory.create( context, AppStartRestInterface.class );
    }

    public void onFirstAppStart( List<Friend> friendList, String profileId ) {
        initRequestBody( friendList, profileId );
        requestBody.setDeviceName( getDeviceName() );
        gcmFirstAppStart();
    }

    public void onRegularAppStart( List<Friend> friendList, String profileId ) {
        initRequestBody( friendList, profileId );
        requestBody.setExceptionVersion( metadataStore.getExceptionVersion() );
        try {
            appStartRestInterface.regularAppStart( requestBody, new Callback<AppStartResponse>() {
                @Override
                public void success( AppStartResponse responseBody, Response response ) {
                    saveCommonData( responseBody );
                }

                @Override
                public void failure( RetrofitError error ) {
                    Toast.makeText( context, context.getString( R.string.failed_to_connect ) + error.getMessage(),
                            Toast.LENGTH_SHORT ).show();
                }
            } );

        } catch ( java.lang.Exception e ) {
            e.printStackTrace();
        }
    }

    private void initRequestBody( List<Friend> friendList, String profileId ) {
        requestBody.setDeviceId( androidId );
        requestBody.setUserFacebookId( profileId );
        requestBody.setFriendsFacebookIds( stream( friendList ).map( Friend::getId ).collect( Collectors.toList() ) );
        requestBody.setKnownExceptionIds( exceptionInstanceManager.getKnownIds() );
        requestBody.setFirstName( yourselfRealm.getYourself().getFirstName() );
        requestBody.setLastName( yourselfRealm.getYourself().getLastName() );
    }

    private void gcmFirstAppStart( ) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground( Void... params ) {
                String message;
                googleCloudMessaging = GoogleCloudMessaging.getInstance( context );
                try {
                    registrationId = googleCloudMessaging.register( projectNumber );
                    message = "Device registered, ID: " + registrationId;
                } catch ( IOException e ) {
                    message = "Error: " + e.getMessage();
                }
                return message;
            }

            @Override
            protected void onPostExecute( String message ) {
                if ( registrationId != null ) {
                    requestBody.setGcmId( registrationId );
                    backendFirstAppStart();
                }
                Toast.makeText( context, message, Toast.LENGTH_SHORT ).show();
            }

        }.execute();
    }

    private void backendFirstAppStart( ) {
        try {
            appStartRestInterface.firstAppStart( requestBody, new Callback<AppStartResponse>() {
                @Override
                public void success( AppStartResponse responseBody, Response response ) {
                    saveCommonData( responseBody );
                    metadataStore.setFirstStartFinished( true );
                }

                @Override
                public void failure( RetrofitError error ) {
                    Toast.makeText( context, context.getString( R.string.failed_to_connect_3 ) + error.getMessage(),
                            Toast.LENGTH_SHORT ).show();
                }
            } );
        } catch ( java.lang.Exception e ) {
            e.printStackTrace();
        }
    }

    private void saveCommonData( AppStartResponse responseBody ) {
        if ( responseBody.getExceptionVersion() > metadataStore.getExceptionVersion() ) {
            exceptionTypeManager.addExceptionTypes( responseBody.getExceptionTypes() );
        }
        metadataStore.setExceptionVersion( responseBody.getExceptionVersion() );
        exceptionInstanceManager.saveExceptionListAsync( responseBody.getMyExceptions() );
        exceptionTypeManager.setVotedExceptionTypes( responseBody.getBeingVotedTypes() );
        metadataStore.setPoints( responseBody.getPoints() );
        metadataStore.setSubmittedThisWeek( responseBody.isSubmittedThisWeek() );
        metadataStore.setVotedThisWeek( responseBody.isVotedThisWeek() );
        friendRealm.updatePointsOfFriendList( responseBody.getFriendsPoints() );
    }

    public String getDeviceName( ) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if ( model.startsWith( manufacturer ) ) {
            return capitalize( model );
        } else {
            return capitalize( manufacturer ) + " " + model;
        }
    }

    private String capitalize( String s ) {
        if ( s == null || s.length() == 0 ) {
            return "";
        }
        char first = s.charAt( 0 );
        if ( Character.isUpperCase( first ) ) {
            return s;
        } else {
            return Character.toUpperCase( first ) + s.substring( 1 );
        }
    }
}
