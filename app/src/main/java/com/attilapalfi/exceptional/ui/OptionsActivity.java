package com.attilapalfi.exceptional.ui;

import javax.inject.Inject;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.persistence.MetadataStore;


public class OptionsActivity extends AppCompatActivity {
    @Inject MetadataStore metadataStore;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );;
        setContentView( R.layout.activity_options );
        ActionBar actionBar = getActionBar();
        if ( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }
    }

    @Override
    public void onBackPressed( ) {
        if ( !metadataStore.getLoggedIn() ) {
            closeApp();
        } else {
            super.onBackPressed();
        }
    }

    private void closeApp( ) {
        Intent intent = new Intent( Intent.ACTION_MAIN );
        intent.addCategory( Intent.CATEGORY_HOME );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

}
