package com.attilapalf.exceptional.ui.main.interfaces;

/**
 * Created by Attila on 2015-06-12.
 */
public interface ExceptionSource {
    boolean addExceptionChangeListener(ExceptionChangeListener listener);
    boolean removeExceptionChangeListener(ExceptionChangeListener listener);
}
