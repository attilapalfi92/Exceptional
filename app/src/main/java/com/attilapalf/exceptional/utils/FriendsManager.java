package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.rest.BackendServiceUser;
import com.attilapalf.exceptional.rest.BackendService;
import com.attilapalf.exceptional.ui.main.interfaces.FriendChangeListener;

import java.io.InputStream;
import java.lang.Exception;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private FriendChangeListener friendChangeListener;

    /**
     * On app start the first thing is to load the storedFriends with data from
     * the preferences file. After that a callback can come from facebook to access
     * storedFriends. If the callback comes too soon, it would be bad, so we synchronize
     * with this object.
     */
    // TODO: not needed
    //private final Object memoryCacheSyncObj = new Object();

    private static FriendsManager instance;

    public static FriendsManager getInstance() {
        if (instance == null) {
            instance = new FriendsManager();
        }

        return instance;
    }

    private FriendsManager() {

    }

    public boolean isInitialized() {
        return sharedPreferences != null;
    }

    public void initialize(Context context) {
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
//        // removing known friends from all friends
//        friendSet.removeAll(storedFriends);
//        // saving the rest of the all friends (these are the new ones)
//        if (!friendSet.isEmpty()) {
//            saveFriends(friendSet);
//            Collections.sort(storedFriends, new Friend.NameComparator());
//        }

        // refreshing all my friends data:

        // these friends are freshly downloaded
        for (Friend f : friendSet) {
            // update friends anyway
            new UpdateFriendTask(f).execute();


//            // this friend is already on the device
//            Friend previous = findFriendById(storedFriends, f);
//            if (previous != null) {
//                // if this friend has changed his/her name or image
//                if (!previous.getImageUrl().equals(f.getImageUrl())
//                    || !previous.getName().equals(f.getName())) {
//
//                    // then we re-add to the storedFriends
//                    storedFriends.remove(previous);
//                    storedFriends.add(f);
//                    editor.putString(Long.toString(f.getId()), f.toString());
//                }
//            }
        }

        editor.apply();
        backendService.onAppStart();
    }


    // TODO: if download failed because of poor internet connection, retry later
    private static class UpdateFriendTask extends AsyncTask<Void, Void, Bitmap> {
        Friend friend;

        public UpdateFriendTask(Friend friend) {
            this.friend = friend;
        }

        @Override
        protected Bitmap doInBackground(Void... param) {
            String downloadUrl = friend.getImageUrl();
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(downloadUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            friend.setImage(bitmap);
            FriendsManager.getInstance().updateFriend(friend);
        }
    }




    private Friend findFriendById(List<Friend> friends, Friend toBeFound) {
        for (Friend iterator : friends) {
            if (iterator.getId() == toBeFound.getId()) {
                return iterator;
            }
        }

        return null;
    }

    public void updateFriend(Friend friend) {
        Friend previous = findFriendById(storedFriends, friend);
        storedFriends.remove(previous);
        storedFriends.add(friend);
        editor.putString(Long.toString(friend.getId()), friend.toString());
        editor.apply();
        if (friendChangeListener != null) {
            friendChangeListener.onFriendsChanged();
        }
    }


    private void saveFriends(Set<Friend> toBeSaved) {
        for (Friend f : toBeSaved) {
            editor.putString(Long.toString(f.getId()), f.toString());
            storedFriends.add(f);
            new UpdateFriendTask(f).execute();
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

    public void setFriendChangeListener(FriendChangeListener listener) {
        friendChangeListener = listener;
    }


    @Override
    public void addBackendService(BackendService service) {
        backendService = service;
    }

    public List<Friend> getStoredFriends() {
        return storedFriends;
    }
}
