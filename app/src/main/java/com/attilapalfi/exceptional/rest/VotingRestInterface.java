package com.attilapalfi.exceptional.rest;

import com.attilapalfi.exceptional.rest.messages.SubmitRequest;
import com.attilapalfi.exceptional.rest.messages.SubmitResponse;
import com.attilapalfi.exceptional.rest.messages.VoteRequest;
import com.attilapalfi.exceptional.rest.messages.VoteResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by palfi on 2015-09-13.
 */
public interface VotingRestInterface {
    @POST( "/voting/vote" )
    void voteForType( @Body VoteRequest request, Callback<VoteResponse> cb );

    @POST( "/voting/submit" )
    void submitTypeForVote( @Body SubmitRequest request, Callback<SubmitResponse> cb );
}
