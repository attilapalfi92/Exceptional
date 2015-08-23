package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.interfaces.ExceptionRefreshListener;

import java.util.List;
import java.util.Set;

/**
 * Created by Attila on 2015-06-13.
 */
public interface BackendService {
    void onFirstAppStart(List<Friend> friendList);
    void onRegularAppStart(List<Friend> friendList);
    void throwException(Exception e);
    void refreshExceptions(final ExceptionRefreshListener refreshListenerParam);
}
