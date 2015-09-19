package com.attilapalfi.exceptional.ui.main;


import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.ExceptionType;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.VotingService;
import com.attilapalfi.exceptional.ui.ExceptionHistoryActivity;
import com.attilapalfi.exceptional.ui.LoginActivity;
import com.attilapalfi.exceptional.ui.OptionsActivity;
import com.attilapalfi.exceptional.ui.main.page_transformers.ZoomOutPageTransformer;

public class MainActivity extends AppCompatActivity {
    private MainPagerAdapter adapter;
    private ViewPager viewPager;
    private View submitView;
    private String submitPrefix = "";
    private String submitShortName = "";
    private String submitDescription = "";
    @Inject VotingService votingService;
    @Inject MetadataStore metadataStore;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Injector.INSTANCE.getApplicationComponent().inject( this );

        setTitle( "Your profile" );

        adapter = new MainPagerAdapter( getSupportFragmentManager(), this );
        viewPager = (ViewPager) findViewById( R.id.main_pager );
        viewPager.setAdapter( adapter );
        viewPager.addOnPageChangeListener( adapter );
        viewPager.setPageTransformer( true, new ZoomOutPageTransformer() );

        Bundle bundle = getIntent().getExtras();
        if ( bundle != null ) {
            int startPage = bundle.getInt( "startPage" );
            if ( startPage != 0 ) {
                viewPager.setCurrentItem( startPage );
            }
        }
    }


    // TODO: Check for Google Play Services APK
    @Override
    protected void onResume( ) {
        super.onResume();

        if ( !metadataStore.isLoggedIn() ) {
            Intent intent = new Intent( this, LoginActivity.class );
            startActivity( intent );
        }
    }


    @Override
    protected void onDestroy( ) {
        super.onDestroy();
        viewPager.removeOnPageChangeListener( adapter );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main_activity2, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if ( id == R.id.action_settings ) {
            Intent intent = new Intent( this, OptionsActivity.class );
            startActivity( intent );
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed( ) {
        Intent intent = new Intent( Intent.ACTION_MAIN );
        intent.addCategory( Intent.CATEGORY_HOME );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );
    }

    public void exceptionHistoryClicked( View view ) {
        Intent intent = new Intent( this, ExceptionHistoryActivity.class );
        startActivity( intent );
    }

    public void submitClicked( View view ) {
        if ( metadataStore.isSubmittedThisWeek() ) {
            Toast.makeText( this, R.string.already_submitted, Toast.LENGTH_SHORT ).show();
        } else {
            MaterialDialog materialDialog = new MaterialDialog.Builder( this )
                    .title( R.string.exception_submitment )
                    .customView( R.layout.submit_layout, true )
                    .positiveText( "Submit" )
                    .negativeText( "Cancel" )
                    .callback( new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive( MaterialDialog dialog ) {
                            getSubmitStrings( dialog.getCustomView() );

                            if ( prefixIsValid() && shortNameIsValid() && descriptionIsValid() ) {
                                votingService.submitType( new ExceptionType(
                                        0,
                                        submitShortName,
                                        submitPrefix,
                                        submitDescription
                                ) );
                            }
                        }
                    } )
                    .build();
            materialDialog.show();
            submitView = materialDialog.getCustomView();
            setSubmitStrings();
        }
    }

    private void setSubmitStrings( ) {
        ( (EditText) submitView.findViewById( R.id.submit_prefix_input ) ).setText( submitPrefix );
        ( (EditText) submitView.findViewById( R.id.submit_shortname_input ) ).setText( submitShortName );
        ( (EditText) submitView.findViewById( R.id.submit_description_input ) ).setText( submitDescription );
    }

    private void getSubmitStrings( View submitView ) {
        submitPrefix = ( (EditText) submitView.findViewById( R.id.submit_prefix_input ) )
                .getText().toString().trim();
        submitShortName = ( (EditText) submitView.findViewById( R.id.submit_shortname_input ) )
                .getText().toString().trim();
        submitDescription = ( (EditText) submitView.findViewById( R.id.submit_description_input ) )
                .getText().toString().trim();
    }

    private boolean prefixIsValid( ) {
        if ( !submitPrefix.endsWith( "." ) ) {
            Toast.makeText( getApplicationContext(), R.string.prefix_end_with_dot_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        if ( submitPrefix.length() < 6 ) {
            Toast.makeText( getApplicationContext(), R.string.prefix_too_short_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        if ( submitPrefix.length() > 500 ) {
            Toast.makeText( getApplicationContext(), R.string.prefix_too_long_error, Toast.LENGTH_SHORT ).show();
        }
        return true;
    }

    private boolean shortNameIsValid( ) {
        if ( submitShortName.length() < 6 ) {
            Toast.makeText( getApplicationContext(), R.string.short_name_too_short_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        if ( submitShortName.length() > 200 ) {
            Toast.makeText( getApplicationContext(), R.string.short_name_too_long_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        return true;
    }

    private boolean descriptionIsValid( ) {
        if ( submitDescription.length() < 12 ) {
            Toast.makeText( getApplicationContext(), R.string.description_too_short_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        if ( submitDescription.length() > 3000 ) {
            Toast.makeText( getApplicationContext(), R.string.description_too_long_error, Toast.LENGTH_SHORT ).show();
            return false;
        }
        return true;
    }
}
