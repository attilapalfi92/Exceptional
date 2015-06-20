package com.attilapalf.exceptional.rest;

/**
 * Created by Attila on 2015-06-14.
 */
public interface ConnectionFailedListener {
    void onConnectionFailed(String what, String why);
}
