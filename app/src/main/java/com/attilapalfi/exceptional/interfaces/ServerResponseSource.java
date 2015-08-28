package com.attilapalfi.exceptional.interfaces;

/**
 * Created by Attila on 2015-06-14.
 */
public interface ServerResponseSource {
    boolean addResponseListener(ServerResponseListener listener);
    boolean removeConnectionListener(ServerResponseListener listener);
}
