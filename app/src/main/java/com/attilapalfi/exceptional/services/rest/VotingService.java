package com.attilapalfi.exceptional.services.rest;

import javax.inject.Inject;

import android.content.Context;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;
import com.attilapalfi.exceptional.services.rest.messages.SubmitRequest;
import com.attilapalfi.exceptional.services.rest.messages.SubmitResponse;
import com.attilapalfi.exceptional.services.rest.messages.VoteRequest;
import com.attilapalfi.exceptional.services.rest.messages.VoteResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by palfi on 2015-09-13.
 */
public class VotingService {
    @Inject Context context;
    @Inject MetadataStore metadataStore;
    @Inject FriendsManager friendsManager;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject RestInterfaceFactory restInterfaceFactory;
    private VotingRestInterface votingRestInterface;

    public VotingService( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        votingRestInterface = restInterfaceFactory.create( context, VotingRestInterface.class );
    }

    public void voteForType( ExceptionType exceptionType ) {
        VoteRequest voteRequest = new VoteRequest( friendsManager.getYourself().getId(), exceptionType.getId() );
        votingRestInterface.voteForType( voteRequest, new Callback<VoteResponse>() {
            @Override
            public void success( VoteResponse voteResponse, Response response ) {
                if ( voteResponse.getVotedForThisWeek() ) {
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
        votingRestInterface.submitTypeForVote( submitRequest, new Callback<SubmitResponse>() {
            @Override
            public void success( SubmitResponse submitResponse, Response response ) {
                if ( submitResponse.getSubmittedThisWeek() ) {
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
