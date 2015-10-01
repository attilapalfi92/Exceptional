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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.model.Submitter;
import com.attilapalfi.exceptional.persistence.ExceptionTypeStore;
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.services.LocationException;
import com.attilapalfi.exceptional.services.LocationProvider;
import com.attilapalfi.exceptional.ui.main.Constants;
import com.attilapalfi.exceptional.ui.main.main_page.MainActivity;

public class ExceptionTypesFragment extends Fragment {
    private static int instanceCounter = 0;

    private List<ExceptionType> exceptionTypes;
    private RecyclerView recyclerView;
    private ExceptionTypeAdapter typeAdapter;
    @Inject
    ExceptionTypeStore exceptionTypeStore;

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
        int index = instanceCounter++ % exceptionTypeStore.getExceptionTypes().size();
        List<String> types = new ArrayList<>( exceptionTypeStore.getExceptionTypes() );
        String typeOfThis = types.get( index );
        exceptionTypes = exceptionTypeStore.getExceptionTypeListByName( typeOfThis );
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
        @Inject
        LocationProvider locationProvider;
        @Inject
        ExceptionFactory exceptionFactory;
        @Inject
        ExceptionService exceptionService;
        @Inject
        FriendStore friendStore;
        @Inject
        MetadataStore metadataStore;

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            private Switch switchView;
            private EditText questionView;
            private RadioButton noRadioView;
            private RadioButton yesRadioView;

            @Override
            public void onClick( View view ) {
                try {
                    Location location = locationProvider.getLocation();
                    Exception exception = createException( view, location );
                    buildDoubleNothingDialog( exception );
                } catch ( LocationException e ) {
                    e.printStackTrace();
                }
            }

            private void buildDoubleNothingDialog( Exception exception ) {
                MaterialDialog.Builder builder = createMaterialDialog( exception );
                setCallbacks( builder, exception );
                MaterialDialog dialog = builder.build();
                setListeners( dialog );
                dialog.show();
            }

            private MaterialDialog.Builder createMaterialDialog( final Exception exception ) {
                return new MaterialDialog.Builder( activity )
                        .title( activity.getString( R.string.throw_question ) + " " + exception.getShortName() + "?" )
                        .customView( R.layout.throw_layout, true )
                        .positiveText( R.string.throwException )
                        .negativeText( R.string.cancel );
            }

            private void setCallbacks( MaterialDialog.Builder builder, Exception exception ) {
                builder.callback( new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive( MaterialDialog dialog ) {
                        if ( inputIsValid() ) {
                            exceptionService.throwException( exception );
                            navigateToMainPage();
                        }
                    }

                    @Override
                    public void onNegative( MaterialDialog dialog ) {
                    }
                } );
            }

            private boolean inputIsValid( ) {
                return false;
            }

            private void setListeners( MaterialDialog dialog ) {
                View throwView = dialog.getCustomView();
                if ( throwView != null ) {
                    switchView = (Switch) throwView.findViewById( R.id.double_or_nothing_switch );
                    switchView.setOnClickListener( this::switchListener );
                    noRadioView = (RadioButton) throwView.findViewById( R.id.double_or_nothing_no_radio );
                    noRadioView.setOnClickListener( this::radioNoListener );
                    yesRadioView = (RadioButton) throwView.findViewById( R.id.double_or_nothing_yes_radio );
                    yesRadioView.setOnClickListener( this::radioYesListener );
                    questionView = (EditText) throwView.findViewById( R.id.double_or_nothing_question_edit_text );
                }
            }

            private void switchListener( View view ) {
                if ( switchView.isChecked() ) {
                    setViewsEnabled( true );
                } else {
                    setViewsEnabled( false );
                }
            }

            private void setViewsEnabled( boolean state ) {
                noRadioView.setEnabled( state );
                yesRadioView.setEnabled( state );
                questionView.setEnabled( state );
            }

            private void radioNoListener( View view ) {
                if ( noRadioView.isChecked() ) {
                    yesRadioView.setChecked( false );
                }
            }

            private void radioYesListener( View view ) {
                if ( yesRadioView.isChecked() ) {
                    noRadioView.setChecked( false );
                }
            }

            private Exception createException( View view, Location location ) {
                int itemPosition = recyclerView.getChildPosition( view );
                Exception exception = createExceptionFromPosition( itemPosition );
                exception.setLatitude( location.getLatitude() );
                exception.setLongitude( location.getLongitude() );
                return exception;
            }

            private Exception createExceptionFromPosition( int itemPosition ) {
                ExceptionType exceptionType = values.get( itemPosition );
                BigInteger friendId = new BigInteger( activity.getIntent().getStringExtra( Constants.FRIEND_ID ) );
                return exceptionFactory.createExceptionWithType(
                        exceptionType,
                        metadataStore.getUser().getId(),
                        friendId );
            }

            private void navigateToMainPage( ) {
                Intent intent = new Intent( activity, MainActivity.class );
                intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                activity.startActivity( intent );
                activity.finish();
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
                Submitter submitter = exceptionType.getSubmitter();
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
