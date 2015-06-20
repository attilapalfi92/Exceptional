package com.attilapalf.exceptional.rest;

import com.attilapalf.exceptional.model.Friend;

import java.util.Set;

/**
 * Created by Attila on 2015-06-13.
 */
public interface BackendService {
    void onFirstAppStart(Set<Friend> friendSet);
    void onAppStart(Set<Friend> friendSet);
}
