package com.attilapalf.exceptional.interfaces;

import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.interfaces.ExceptionRefreshListener;

import java.util.Set;

/**
 * Created by Attila on 2015-06-13.
 */
public interface BackendService {
    void onFirstAppStart(Set<Friend> friendSet);
    void onAppStart();
    void sendException(Exception e);
    void refreshExceptions(final ExceptionRefreshListener refreshListenerParam);
}
