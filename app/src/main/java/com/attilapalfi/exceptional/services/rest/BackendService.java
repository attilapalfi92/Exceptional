package com.attilapalfi.exceptional.services.rest;

import javax.inject.Inject;

import android.content.Context;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.ExceptionFactory;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.rest.messages.*;
import com.google.gson.GsonBuilder;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendService {
    @Inject Context context;
    @Inject ExceptionInstanceManager exceptionInstanceManager;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject ExceptionFactory exceptionFactory;
    @Inject FriendsManager friendsManager;
    @Inject MetadataStore metadataStore;
    private RestInterface restInterface;

    public BackendService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint( context.getString( R.string.backend_address ) )
                .setConverter( new GsonConverter( ( new GsonBuilder().create() ) ) )
                .build();
        restInterface = restAdapter.create( RestInterface.class );
    }

    public void throwException( Exception exception ) {
        ExceptionInstanceWrapper exceptionInstanceWrapper = new ExceptionInstanceWrapper( exception );
        try {
            restInterface.throwException( exceptionInstanceWrapper, new Callback<ExceptionSentResponse>() {
                @Override
                public void success( ExceptionSentResponse e, Response response ) {
                    Friend toWho = friendsManager.findFriendById( e.getInstanceWrapper().getToWho() );
                    metadataStore.setPoints( e.getYourPoints() );
                    friendsManager.updateFriendPoints( e.getInstanceWrapper().getToWho(), e.getFriendsPoints() );
                    exceptionInstanceManager.addExceptionAsync( exceptionFactory.createFromWrapper( e.getInstanceWrapper() ) );
                    Toast.makeText( context, e.getExceptionShortName() + " "
                                    + context.getString( R.string.successfully_thrown )
                                    + " " + toWho.getName(),
                            Toast.LENGTH_SHORT ).show();
                }

                @Override
                public void failure( RetrofitError error ) {
                    Toast.makeText( context, context.getString( R.string.failed_to_throw_1 ) + error.getMessage(),
                            Toast.LENGTH_SHORT ).show();
                }
            } );

        } catch ( java.lang.Exception e ) {
            Toast.makeText( context, context.getString( R.string.failed_to_throw_2 ) + e.getMessage(), Toast.LENGTH_SHORT ).show();
        }
    }

    public void refreshExceptions( final ExceptionRefreshListener refreshListener ) {
        BaseExceptionRequest requestBody = new BaseExceptionRequest(
                friendsManager.getYourself().getId(),
                exceptionInstanceManager.getExceptionList()
        );
        restInterface.refreshExceptions( requestBody, new Callback<ExceptionRefreshResponse>() {

            @Override
            public void success( ExceptionRefreshResponse exceptionRefreshResponse, Response response ) {
                exceptionInstanceManager.saveExceptionListAsync( exceptionRefreshResponse.getExceptionList() );
                Toast.makeText( context, R.string.exceptions_syncd, Toast.LENGTH_SHORT ).show();
                refreshListener.onExceptionRefreshFinished();
            }

            @Override
            public void failure( RetrofitError error ) {
                Toast.makeText( context, context.getString( R.string.failed_to_sync ) + error.getMessage(), Toast.LENGTH_SHORT ).show();
                refreshListener.onExceptionRefreshFinished();
            }
        } );
    }

    public void voteForType( ExceptionType exceptionType ) {
        VoteRequest voteRequest = new VoteRequest( friendsManager.getYourself().getId(), exceptionType.getId() );
        restInterface.voteForType( voteRequest, new Callback<VoteResponse>() {
            @Override
            public void success( VoteResponse voteResponse, Response response ) {
                if ( voteResponse.isVotedForThisWeek() ) {
                    metadataStore.setVotedThisWeek( true );
                    exceptionTypeManager.updateVotedType( voteResponse.getVotedType() );
                }
            }

            @Override
            public void failure( RetrofitError error ) {
                Toast.makeText( context, R.string.failed_to_vote, Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    public void submitType( ExceptionType submittedType ) {
        SubmitRequest submitRequest = new SubmitRequest( friendsManager.getYourself().getId(), submittedType );
        restInterface.submitTypeForVote( submitRequest, new Callback<SubmitResponse>() {
            @Override
            public void success( SubmitResponse submitResponse, Response response ) {
                if ( submitResponse.isSubmittedThisWeek() ) {
                    metadataStore.setSubmittedThisWeek( true );
                    exceptionTypeManager.addVotedType( submitResponse.getSubmittedType() );
                }
            }

            @Override
            public void failure( RetrofitError error ) {
                Toast.makeText( context, R.string.failed_to_submit, Toast.LENGTH_SHORT ).show();
            }
        } );
    }
}
