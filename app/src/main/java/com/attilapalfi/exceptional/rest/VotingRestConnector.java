package com.attilapalfi.exceptional.rest;

import javax.inject.Inject;

import android.content.Context;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore;
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.messages.SubmitRequest;
import com.attilapalfi.exceptional.rest.messages.SubmitResponse;
import com.attilapalfi.exceptional.rest.messages.VoteRequest;
import com.attilapalfi.exceptional.rest.messages.VoteResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by palfi on 2015-09-13.
 */
public class VotingRestConnector {
    @Inject Context context;
    @Inject MetadataStore metadataStore;
    @Inject FriendStore friendStore;
    @Inject ExceptionTypeStore exceptionTypeStore;
    @Inject RestInterfaceFactory restInterfaceFactory;
    private VotingRestInterface votingRestInterface;

    public VotingRestConnector( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        votingRestInterface = restInterfaceFactory.create( context, VotingRestInterface.class );
    }

    public void voteForType( ExceptionType exceptionType ) {
        VoteRequest voteRequest = new VoteRequest( metadataStore.getUser().getId(), exceptionType.getId() );
        votingRestInterface.voteForType( voteRequest, new Callback<VoteResponse>() {
            @Override
            public void success( VoteResponse voteResponse, Response response ) {
                if ( voteResponse.getVotedForThisWeek() ) {
                    metadataStore.setVotedThisWeek( true );
                    exceptionTypeStore.updateVotedType( voteResponse.getVotedType() );
                }
            }

            @Override
            public void failure( RetrofitError error ) {
                Toast.makeText( context, R.string.failed_to_vote, Toast.LENGTH_LONG ).show();
            }
        } );
    }

    public void submitType( ExceptionType submittedType ) {
        SubmitRequest submitRequest = new SubmitRequest( metadataStore.getUser().getId(), submittedType );
        votingRestInterface.submitTypeForVote( submitRequest, new Callback<SubmitResponse>() {
            @Override
            public void success( SubmitResponse submitResponse, Response response ) {
                if ( submitResponse.getSubmittedThisWeek() ) {
                    metadataStore.setSubmittedThisWeek( true );
                    exceptionTypeStore.addVotedType( submitResponse.getSubmittedType() );
                }
            }

            @Override
            public void failure( RetrofitError error ) {
                Toast.makeText( context, R.string.failed_to_submit, Toast.LENGTH_LONG ).show();
            }
        } );
    }
}
