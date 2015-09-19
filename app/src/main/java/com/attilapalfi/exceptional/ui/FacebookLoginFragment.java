package com.attilapalfi.exceptional.ui;

import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.facebook.FacebookLoginSuccessHandler;
import com.attilapalfi.exceptional.facebook.FacebookManager;
import com.attilapalfi.exceptional.ui.main.MainActivity;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


/**
 */
public class FacebookLoginFragment extends Fragment implements FacebookLoginSuccessHandler {

    private TextView welcomeText;
    @Inject FacebookManager facebookManager;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_fb_login, container, false );

        facebookManager.registerLoginSuccessHandler( this );

        LoginButton loginButton = (LoginButton) view.findViewById( R.id.login_button );
        loginButton.setReadPermissions( "user_friends" );
        // If using in a fragment
        loginButton.setFragment( this );

        loginButton.setReadPermissions( "user_friends" );
        loginButton.registerCallback( facebookManager.getCallbackManager(),
                facebookManager.getFacebookCallback() );

        welcomeText = (TextView) view.findViewById( R.id.welcomeText );

        return view;
    }


    @Override
    public void onLoginSuccess( LoginResult loginResult ) {
        Intent intent = new Intent( getActivity(), MainActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
    }


    private void displayWelcomeMessage( Profile profile ) {
        if ( profile != null ) {
            welcomeText.setText( "Welcome " + profile.getFirstName() );
        }
    }


    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        facebookManager.onActivityResult( requestCode, resultCode, data );
    }

    @Override
    public void onResume( ) {
        super.onResume();
        displayWelcomeMessage( Profile.getCurrentProfile() );
    }
}
