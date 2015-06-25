package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.rest.BackendServiceUser;
import com.attilapalf.exceptional.rest.BackendService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendsManager implements FacebookManager.FriendListListener, BackendServiceUser {

    private BackendService backendService;

    /** This is the application's preferences */
    private SharedPreferences sharedPreferences;

    /** This is the application's sharedPreferences editor*/
    private SharedPreferences.Editor editor;

//    private final Set<Friend> storedFriends = Collections.synchronizedSet(
//            new TreeSet<Friend>(new Friend.NameComparator()));
    private final List<Friend> storedFriends = new LinkedList<>();


    /**
     * On app start the first thing is to load the storedFriends with data from
     * the preferences file. After that a callback can come from facebook to access
     * storedFriends. If the callback comes too soon, it would be bad, so we synchronize
     * with this object.
     */
    // TODO: not needed
    //private final Object memoryCacheSyncObj = new Object();

    private static FriendsManager instance;

    public static FriendsManager getInstance(Context context) {
        if (instance == null) {
            instance = new FriendsManager(context);
        }

        return instance;
    }

    private FriendsManager(Context context) {
        String PREFS_NAME = context.getString(R.string.friends_preferences);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.apply();

        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> keys = store.keySet();

        String[] keyArray = new String[keys.size()];
        keys.toArray(keyArray);
        //synchronized (memoryCacheSyncObj) {
            for (String s : keyArray) {
                String friendJson = (String) store.get(s);
                Friend f = Friend.fromString(friendJson);
                storedFriends.add(f);
            }
        //}
        //Collections.sort(storedFriends, new Friend.NameComparator());
    }


    @Override
    public void onFirstAppStart(Set<Friend> friendSet) {
        //synchronized (memoryCacheSyncObj) {
            // adding facebook friends
            saveFriends(friendSet);

            // TODO: send facebookFriends to backend database
            backendService.onFirstAppStart(friendSet);
        //}
    }


    @Override
    public void onAppStart(Set<Friend> friendSet) {
        // removing known friends from all friends
        friendSet.removeAll(storedFriends);
        // saving the rest of the all friends (these are the new ones)
        if (!friendSet.isEmpty()) {
            saveFriends(friendSet);
            Collections.sort(storedFriends, new Friend.NameComparator());
        }

        backendService.onAppStart();
    }





    private void saveFriends(Set<Friend> toBeSaved) {
        for (Friend f : toBeSaved) {
            editor.putString(Long.toString(f.getId()), f.toString());
            storedFriends.add(f);
        }
        editor.apply();
    }


    private void deleteFriends(Set<Friend> toBeDeleted) {
        for (Friend f : toBeDeleted) {
            editor.remove(Long.toString(f.getId()));
        }
        editor.apply();
        storedFriends.removeAll(toBeDeleted);
    }




    @Override
    public void addBackendService(BackendService service) {
        backendService = service;
    }

    public List<Friend> getStoredFriends() {
        return storedFriends;
    }
}
