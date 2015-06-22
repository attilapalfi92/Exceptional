package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.rest.BackendServiceUser;
import com.attilapalf.exceptional.rest.BackendService;

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
    private final Set<Friend> storedFriends = new TreeSet<>(new Friend.NameComparator());


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
        saveFriends(friendSet);

        backendService.onAppStart();
    }



//    public void addFacebookFriends(Set<Friend> facebookFriends) {
//        Set<Friend> storedTemporary = new TreeSet<>(new Friend.NameComparator());
//        storedTemporary.addAll(storedFriends);
//        // these have to be deleted
//        storedTemporary.removeAll(facebookFriends);
//
//        Set<Friend> fbTemporaryFriends = new TreeSet<>(new Friend.NameComparator());
//        fbTemporaryFriends.addAll(facebookFriends);
//        // these have to be added to the stored friends
//        fbTemporaryFriends.removeAll(storedFriends);
//
//        // removing deleted friends
//        deleteFriends(storedTemporary);
//
//        // adding new friends
//        saveFriends(fbTemporaryFriends);
//
//        // TODO: send facebookFriends to backend database

//
//        // TODO: notify FriendsFragment's adapter about the data set change
//    }









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
}
