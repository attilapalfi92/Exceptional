package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.ui.main.ExceptionChangeListener;
import com.attilapalf.exceptional.ui.main.ExceptionSource;

/**
 * Created by Attila on 2015-06-13.
 */
public class BackendConnector implements BackendService, ExceptionSource {
    @Override
    public void onFirstAppStart() {

    }

    @Override
    public void onAppStart() {

    }

    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        return false;
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        return false;
    }
}
