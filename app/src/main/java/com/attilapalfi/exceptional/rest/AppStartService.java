package com.attilapalfi.exceptional.rest;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.persistence.*;
import com.attilapalfi.exceptional.rest.messages.AppStartRequest;
import com.attilapalfi.exceptional.rest.messages.AppStartResponse;
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
    @Inject
    Context context;
    @Inject
    ExceptionInstanceStore exceptionInstanceStore;
    @Inject
    ExceptionTypeStore exceptionTypeStore;
    @Inject
    FriendStore friendStore;
    @Inject
    MetadataStore metadataStore;
    @Inject
    RestInterfaceFactory restInterfaceFactory;
    @Inject
    QuestionStore questionStore;
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

    public void onFirstAppStart( List<Friend> friendList, BigInteger profileId ) {
        initRequestBody( friendList, profileId );
        requestBody.setDeviceName( getDeviceName() );
        gcmFirstAppStart();
    }

    public void onRegularAppStart( List<Friend> friendList, BigInteger profileId ) {
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
                            Toast.LENGTH_LONG ).show();
                }
            } );

        } catch ( java.lang.Exception e ) {
            e.printStackTrace();
        }
    }

    private void initRequestBody( List<Friend> friendList, BigInteger profileId ) {
        requestBody.setDeviceId( androidId );
        requestBody.setUserFacebookId( profileId );
        requestBody.setFriendsFacebookIds( stream( friendList ).map( Friend::getId ).collect( Collectors.toList() ) );
        requestBody.setKnownExceptionIds( exceptionInstanceStore.getKnownIds() );
        requestBody.setFirstName( metadataStore.getUser().getFirstName() );
        requestBody.setLastName( metadataStore.getUser().getLastName() );
    }

    private void gcmFirstAppStart( ) {
        googleCloudMessaging = GoogleCloudMessaging.getInstance( context );
        try {
            registrationId = googleCloudMessaging.register( projectNumber );
        } catch ( IOException e ) {
            Toast.makeText( context, context.getString( R.string.failed_to_connect_to_gcm_servers )
                    + e.getMessage(), Toast.LENGTH_LONG ).show();
        }
        if ( registrationId != null ) {
            requestBody.setGcmId( registrationId );
            backendFirstAppStart();
        }
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
                            Toast.LENGTH_LONG ).show();
                }
            } );
        } catch ( java.lang.Exception e ) {
            e.printStackTrace();
        }
    }

    private void saveCommonData( AppStartResponse responseBody ) {
        new Thread( ( ) -> {
            if ( responseBody.getExceptionVersion() > metadataStore.getExceptionVersion() ) {
                exceptionTypeStore.addExceptionTypes( responseBody.getExceptionTypes() );
            }
            friendStore.updatePointsOfFriends( responseBody.getFriendsPoints() );
            metadataStore.setPoints( responseBody.getPoints() );
            metadataStore.setSubmittedThisWeek( responseBody.getSubmittedThisWeek() );
            metadataStore.setVotedThisWeek( responseBody.getVotedThisWeek() );
            metadataStore.setExceptionVersion( responseBody.getExceptionVersion() );
            exceptionTypeStore.setVotedExceptionTypes( responseBody.getBeingVotedTypes() );
            questionStore.addQuestionList( responseBody.getQuestionExceptions() );
            exceptionInstanceStore.saveExceptionList( responseBody.getMyExceptions() );
        } ).start();
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
