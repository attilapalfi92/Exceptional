package com.attilapalf.exceptional.services.facebook;

import com.attilapalf.exceptional.model.Friend;

import java.util.List;
import java.util.Set;

/**
 * Created by 212461305 on 2015.07.07..
 *
 * When the user starts the application, first thing is that it sends a request to
 * Facebook to get his friends. When facebook sends his friends back, these methods are called.
 */
public interface FacebookEventListener {
    void onFirstAppStart(List<Friend> friendList, Friend yourself);
    void onRegularAppStart(List<Friend> friendList, Friend yourself);
    void onNoInternetStart();
}
