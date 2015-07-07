package com.attilapalf.exceptional.ui;

import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.ui.main.MainActivity;
import com.attilapalf.exceptional.services.FacebookManager;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


/**
 */
public class FbLoginFragment extends Fragment implements FacebookManager.LoginSuccessHandler {

    private TextView welcomeText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fb_login, container, false);

        FacebookManager.getInstance().registerLoginSuccessHandler(this);

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        // If using in a fragment
        loginButton.setFragment(this);

        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(FacebookManager.getInstance().getCallbackManager(),
                FacebookManager.getInstance().getFacebookCallback());

        welcomeText = (TextView) view.findViewById(R.id.welcomeText);

        return view;
    }


    @Override
    public void onLoginSuccess(LoginResult loginResult) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
    }


    private void displayWelcomeMessage(Profile profile) {
        if (profile != null) {
            welcomeText.setText("Welcome " + profile.getFirstName());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FacebookManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayWelcomeMessage(Profile.getCurrentProfile());
    }
}
