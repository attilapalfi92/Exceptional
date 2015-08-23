package com.attilapalfi.exceptional.interfaces;

/**
 * Created by Attila on 2015-06-14.
 */
public interface ServerResponseListener {
    void onConnectionFailed(String what, String why);
    void onSuccess(String message);
}
