package com.attilapalf.exceptional.rest;

/**
 * Created by Attila on 2015-06-14.
 */
public interface ConnectionFailedSource {
    boolean addConnectionListener(ConnectionFailedListener listener);
    boolean removeConnectionListener(ConnectionFailedListener listener);
}
