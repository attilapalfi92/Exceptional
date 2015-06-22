package com.attilapalf.exceptional.rest;

/**
 * Created by Attila on 2015-06-14.
 */
public interface ServerResponseSource {
    boolean addConnectionListener(ServerResponseListener listener);
    boolean removeConnectionListener(ServerResponseListener listener);
}
