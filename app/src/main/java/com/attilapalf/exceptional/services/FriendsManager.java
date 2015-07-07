package com.attilapalf.exceptional.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.interfaces.BackendServiceUser;
import com.attilapalf.exceptional.interfaces.BackendService;
import com.attilapalf.exceptional.interfaces.ApplicationStartupListener;
import com.attilapalf.exceptional.interfaces.FriendChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendsManager implements ApplicationStartupListener, BackendServiceUser {

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
        Set<Friend> friendSetCopy = new HashSet<>(friendSet.size());
        friendSetCopy.addAll(friendSet);

        // new friends
        friendSet.removeAll(storedFriends);

        // checking for changes at old friends
        for (Friend f1 : friendSetCopy) {
            for (Friend f2 : storedFriends) {

                // if they are the same
                if (f1.getId() == f2.getId()) {

                    // if his image is changed
                    if (!f1.getImageUrl().equals(f2.getImageUrl())) {
                        new UpdateFriendsImageTask(f2).execute();
                    }

                    // if his name is changed
                    if (!f1.getName().equals(f2.getName())) {
                        updateFriend(f2);
                    }

                }
            }
        }

        // handling new friends
        for (Friend f : friendSet) {
            new UpdateFriendsImageTask(f).execute();
        }

        editor.apply();
        backendService.onAppStart();
    }


    @Override
    public void onNoInternetStart() {
//        for (Friend friend : storedFriends) {
//            new UpdateFriendsImageTask(friend).execute();
//        }
    }



    // TODO: if download failed because of poor internet connection, retry later
    private static class UpdateFriendsImageTask extends AsyncTask<Void, Void, Bitmap> {
        Friend friend;

        public UpdateFriendsImageTask(Friend friend) {
            this.friend = friend;
        }

        @Override
        protected Bitmap doInBackground(Void... param) {
            String downloadUrl = friend.getImageUrl();
            Bitmap friendPicture = null;


            try {
                friendPicture = ImageCache.getInstance().getImage(friend);

                if (friendPicture == null) {
                    URL url = new URL(downloadUrl);
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(true);
                    InputStream inputStream = (InputStream) connection.getContent();
                    friendPicture = BitmapFactory.decodeStream(inputStream);

                    ImageCache.getInstance().addImage(friend, friendPicture);
                }

            } catch (IOException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return friendPicture;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            friend.setImage(bitmap);
            FriendsManager.getInstance().updateFriend(friend);
        }
    }




    public Friend findFriendById(long friendId) {
        for (Friend iterator : storedFriends) {
            if (iterator.getId() == friendId) {
                return iterator;
            }
        }

        return null;
    }


    public void updateFriend(Friend friend) {
        Friend previous = findFriendById(friend.getId());
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
            new UpdateFriendsImageTask(f).execute();
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
