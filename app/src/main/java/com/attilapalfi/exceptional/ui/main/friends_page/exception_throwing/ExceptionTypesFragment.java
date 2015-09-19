package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.services.GpsService;
import com.attilapalfi.exceptional.persistence.ExceptionTypeManager;
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.MainActivity;

public class ExceptionTypesFragment extends Fragment {
    private static int instanceCounter = 0;

    private List<ExceptionType> exceptionTypes;
    private RecyclerView recyclerView;
    private ExceptionTypeAdapter typeAdapter;
    @Inject ExceptionTypeManager exceptionTypeManager;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        initExceptionTypes();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        initTypeAdapter();
        View view = initRecyclerView( inflater, container );
        typeAdapter.notifyDataSetChanged();
        return view;
    }

    private void initExceptionTypes( ) {
        int index = instanceCounter++ % exceptionTypeManager.getExceptionTypes().size();
        List<String> types = new ArrayList<>( exceptionTypeManager.getExceptionTypes() );
        String typeOfThis = types.get( index );
        exceptionTypes = exceptionTypeManager.getExceptionTypeListByName( typeOfThis );
    }

    private void initTypeAdapter( ) {
        if ( exceptionTypes == null ) {
            exceptionTypes = new ArrayList<>();
        }
        typeAdapter = new ExceptionTypeAdapter( getActivity(), exceptionTypes );
    }

    @NonNull
    private View initRecyclerView( LayoutInflater inflater, ViewGroup container ) {
        View view = inflater.inflate( R.layout.fragment_exception_types, container, false );
        recyclerView = (RecyclerView) view.findViewById( R.id.exception_type_recycler_view );
        recyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        recyclerView.setAdapter( typeAdapter );
        typeAdapter.setRecyclerView( recyclerView );
        return view;
    }

    public static class ExceptionTypeAdapter extends RecyclerView.Adapter<ExceptionTypeAdapter.RowViewHolder> {
        private RecyclerView recyclerView;
        private Activity activity;
        private List<ExceptionType> values;
        @Inject GpsService gpsService;
        @Inject ExceptionFactory exceptionFactory;
        @Inject ExceptionService exceptionService;
        @Inject FriendStore friendStore;
        @Inject MetadataStore metadataStore;

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if ( gpsService.canGetLocation() ) {
                    Exception exception = createException( view );
                    exceptionService.throwException( exception );
                    navigateToMainPage();
                } else {
                    Toast.makeText( activity.getApplicationContext(), R.string.can_throw_location_pls,
                            Toast.LENGTH_LONG ).show();
                }
            }

            private Exception createException( View view ) {
                int itemPosition = recyclerView.getChildPosition( view );
                ExceptionType exceptionType = values.get( itemPosition );
                BigInteger friendId = new BigInteger( activity.getIntent().getStringExtra( Constants.FRIEND_ID ) );
                Exception exception = exceptionFactory.createExceptionWithType(
                        exceptionType,
                        metadataStore.getUser().getId(),
                        friendId );
                setLocationForException( exception );
                return exception;
            }

            private void navigateToMainPage( ) {
                Intent intent = new Intent( activity, MainActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                activity.startActivity( intent );
                activity.finish();
            }

            private void setLocationForException( Exception exception ) {
                Location location = gpsService.getLocation();
                exception.setLatitude( location.getLatitude() );
                exception.setLongitude( location.getLongitude() );
            }
        };

        public ExceptionTypeAdapter( Activity activity, List<ExceptionType> values ) {
            this.activity = activity;
            this.values = values;
            Injector.INSTANCE.getApplicationComponent().inject( this );
        }

        @Override
        public RowViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.exception_type_row_layout, parent, false );
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
            private TextView shortNameView;
            private TextView fullNameView;
            private TextView descriptionView;
            private TextView submitterView;

            public RowViewHolder( View itemView, Context context ) {
                super( itemView );
                this.context = context;
                shortNameView = (TextView) itemView.findViewById( R.id.type_short_name_text );
                fullNameView = (TextView) itemView.findViewById( R.id.type_full_name_text );
                descriptionView = (TextView) itemView.findViewById( R.id.type_description_text );
                submitterView = (TextView) itemView.findViewById( R.id.type_submitter_text );
            }

            public void bindRow( ExceptionType exceptionType ) {
                shortNameView.setText( exceptionType.getShortName() );
                String fullName = getFullName( exceptionType );
                fullNameView.setText( fullName );
                descriptionView.setText( exceptionType.getDescription() );
                bindSubmitter( exceptionType );
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
