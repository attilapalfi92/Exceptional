package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.ui.main.ExceptionChangeListener;
import com.attilapalf.exceptional.ui.main.ExceptionSource;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendConnector implements BackendService, ExceptionSource {

    private static BackendConnector instance;

    public static BackendConnector getInstance() {
        if (instance == null) {
            instance = new BackendConnector();
        }

        return instance;
    }

    private BackendConnector(){
        exceptionChangeListeners = new HashSet<>();
    }

    private Set<ExceptionChangeListener> exceptionChangeListeners;

    @Override
    public void onFirstAppStart() {

    }

    @Override
    public void onAppStart() {

    }

    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        if (exceptionChangeListeners == null) {
            exceptionChangeListeners = new HashSet<>();
        }
        return exceptionChangeListeners.add(listener);
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        if (exceptionChangeListeners == null) {
            exceptionChangeListeners = new HashSet<>();
        }
        return exceptionChangeListeners.remove(listener);
    }
}
