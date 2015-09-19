package com.attilapalfi.exceptional.ui.main;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.VotedTypeListener;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.persistence.ExceptionTypeManager;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.VotingService;

/**
 * Created by palfi on 2015-09-05.
 */
public class VotedExceptionsFragment extends Fragment implements VotedTypeListener {
    private VotedExceptionAdapter adapter;
    private RecyclerView recyclerView;
    private List<ExceptionType> votedTypeList;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject VotingService votingService;
    @Inject MetadataStore metadataStore;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        exceptionTypeManager.addVotedTypeListener( this );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        initTypeAdapter();
        View view = initRecyclerView( inflater, container );
        adapter.notifyDataSetChanged();
        return view;
    }

    @Override
    public void onDestroy( ) {
        exceptionTypeManager.removeVotedTypeListener( this );
        super.onDestroy();
    }

    private void initTypeAdapter( ) {
        votedTypeList = exceptionTypeManager.getVotedExceptionTypeList();
        if ( votedTypeList == null ) {
            votedTypeList = new ArrayList<>();
        }
        adapter = new VotedExceptionAdapter( getActivity(), votedTypeList, votingService, metadataStore );
    }

    @NonNull
    private View initRecyclerView( LayoutInflater inflater, ViewGroup container ) {
        View view = inflater.inflate( R.layout.fragment_voted_exceptions, container, false );
        recyclerView = (RecyclerView) view.findViewById( R.id.voted_exceptions_recycler_view );
        recyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        recyclerView.setAdapter( adapter );
        adapter.setRecyclerView( recyclerView );
        return view;
    }

    @Override
    public void onVoteListChanged( ) {
        adapter.setValues( exceptionTypeManager.getVotedExceptionTypeList() );
        adapter.notifyDataSetChanged();
    }

    public static class VotedExceptionAdapter extends RecyclerView.Adapter<VotedExceptionAdapter.RowViewHolder> {
        private RecyclerView recyclerView;
        private Activity activity;
        private List<ExceptionType> values;
        private VotingService votingService;
        private MetadataStore metadataStore;

        private OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick( View view ) {
                int itemPosition = recyclerView.getChildPosition( view );
                ExceptionType exceptionType = values.get( itemPosition );
                if ( metadataStore.isVotedThisWeek() ) {
                    Toast.makeText( activity, R.string.you_already_voted, Toast.LENGTH_SHORT ).show();
                } else {
                    showDialog( exceptionType );
                }
            }

            private void showDialog( ExceptionType exceptionType ) {
                String data = exceptionType.getPrefix() + "\n" +
                        exceptionType.getShortName() + "\n\n" +
                        exceptionType.getDescription() + "\n\n" +
                        "by: " + exceptionType.getSubmitter().fullName();

                new MaterialDialog.Builder( activity )
                        .title( R.string.do_want_to_vote )
                        .content( data )
                        .positiveText( R.string.vote )
                        .negativeText( R.string.cancel )
                        .callback( new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive( MaterialDialog dialog ) {
                                votingService.voteForType( exceptionType );
                            }
                        } )
                        .show();
            }
        };

        public void setValues( List<ExceptionType> values ) {
            this.values = values;
        }

        public VotedExceptionAdapter( Activity activity, List<ExceptionType> votedTypeList,
                                      VotingService votingService, MetadataStore metadataStore ) {
            this.activity = activity;
            this.values = votedTypeList;
            this.votingService = votingService;
            this.metadataStore = metadataStore;
        }

        @Override
        public RowViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.voted_exception_row_layout, parent, false );
            view.setOnClickListener( onClickListener );
            return new RowViewHolder( view, activity.getApplicationContext() );
        }

        @Override
        public void onBindViewHolder( RowViewHolder holder, int position ) {
            holder.bindRow( values.get( position ) );
        }

        @Override
        public int getItemCount( ) {
            return values != null ? values.size() : 0;
        }

        public void setRecyclerView( RecyclerView recyclerView ) {
            this.recyclerView = recyclerView;
        }

        public static class RowViewHolder extends RecyclerView.ViewHolder {
            private Context context;
            private TextView voteCountView;
            private TextView shortNameView;
            private TextView fullNameView;
            private TextView descriptionView;
            private TextView submitterView;

            public RowViewHolder( View viewItem, Context context ) {
                super( viewItem );
                this.context = context;
                voteCountView = (TextView) itemView.findViewById( R.id.voted_vote_count );
                shortNameView = (TextView) itemView.findViewById( R.id.voted_short_name_text );
                fullNameView = (TextView) itemView.findViewById( R.id.voted_full_name_text );
                descriptionView = (TextView) itemView.findViewById( R.id.voted_description_text );
                submitterView = (TextView) itemView.findViewById( R.id.voted_submitter_text );
            }

            public void bindRow( ExceptionType votedType ) {
                voteCountView.setText( context.getString( R.string.voted_type_votes_text ) + votedType.getVoteCount() );
                shortNameView.setText( votedType.getShortName() );
                String fullName = getFullName( votedType );
                fullNameView.setText( fullName );
                descriptionView.setText( votedType.getDescription() );
                bindSubmitter( votedType );
            }

            private void bindSubmitter( ExceptionType exceptionType ) {
                ExceptionType.Submitter submitter = exceptionType.getSubmitter();
                String submitterString = context.getResources().getString( R.string.submitter_text );
                if ( submitter != null ) {
                    submitterString += submitter.getFirstName() + " " + submitter.getLastName();
                } else {
                    submitterString += "System";
                }
                submitterView.setText( submitterString );
            }

            @NonNull
            private String getFullName( ExceptionType exceptionType ) {
                String fullNameParts[] = exceptionType.fullName().split( "\\." );
                String fullName = "";
                for ( String part : fullNameParts ) {
                    fullName += part + "." + "\n";
                }
                fullName = fullName.substring( 0, fullName.length() - 2 );
                return fullName;
            }
        }

    }
}
