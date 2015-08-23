package com.attilapalfi.exceptional.interfaces;

/**
 * Created by Attila on 2015-06-20.
 */
public interface FriendSource {
    boolean addFriendChangeListener(FriendChangeListener listener);
    boolean removeFriendChangeListener(FriendChangeListener listener);
}
