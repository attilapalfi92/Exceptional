package com.attilapalfi.exceptional.services.rest;

import javax.inject.Inject;

import android.content.Context;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.ExceptionFactory;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendRealm;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.persistent_stores.YourselfRealm;
import com.attilapalfi.exceptional.services.rest.messages.BaseExceptionRequest;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionRefreshResponse;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionSentResponse;
import io.realm.Realm;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Attila on 2015-06-13.
 */
public class ExceptionService {
    @Inject Context context;
    @Inject ExceptionInstanceManager exceptionInstanceManager;
    @Inject ExceptionFactory exceptionFactory;
    @Inject FriendRealm friendRealm;
    @Inject YourselfRealm yourselfRealm;
    @Inject MetadataStore metadataStore;
    @Inject RestInterfaceFactory restInterfaceFactory;
    private ExceptionRestInterface exceptionRestInterface;

    public ExceptionService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        exceptionRestInterface = restInterfaceFactory.create( context, ExceptionRestInterface.class );
    }

    public void throwException( Exception exception ) {
        ExceptionInstanceWrapper exceptionInstanceWrapper = new ExceptionInstanceWrapper( exception );
        try {
            exceptionRestInterface.throwException( exceptionInstanceWrapper, new Callback<ExceptionSentResponse>() {
                @Override
                public void success( ExceptionSentResponse e, Response response ) {
                    try ( Realm realm = Realm.getInstance( context ) ) {
                        Friend toWho = realm.where( Friend.class ).equalTo( "id", e.getInstanceWrapper().getToWho() ).findFirst();
                        metadataStore.setPoints( e.getYourPoints() );
                        friendRealm.updateFriendPoints( e.getInstanceWrapper().getToWho(), e.getFriendsPoints() );
                        exceptionInstanceManager.addExceptionAsync( exceptionFactory.createFromWrapper( e.getInstanceWrapper() ) );
                        Toast.makeText( context, e.getExceptionShortName() + " "
                                        + context.getString( R.string.successfully_thrown )
                                        + " " + toWho.getFirstName() + " " + toWho.getLastName(),
                                Toast.LENGTH_SHORT ).show();
                    }
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
                yourselfRealm.getYourself().getId(),
                exceptionInstanceManager.getExceptionList()
        );
        exceptionRestInterface.refreshExceptions( requestBody, new Callback<ExceptionRefreshResponse>() {

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
}
