package com.attilapalfi.exceptional.facebook;

import com.facebook.login.LoginResult;

/**
 * Created by palfi on 2015-08-19.
 */
public interface FacebookLoginSuccessHandler {
    void onLoginSuccess( LoginResult loginResult );
}
