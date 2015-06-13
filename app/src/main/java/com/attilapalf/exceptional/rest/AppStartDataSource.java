package com.attilapalf.exceptional.rest;

/**
 * Created by Attila on 2015-06-13.
 */
public interface AppStartDataSource {
    boolean addAppStartHandler(AppStartHandler handler);
    boolean removeAppStartHandler(AppStartHandler handler);
}
