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
import java.util.ArrayList;
import java.util.Collections;
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
    private Friend yourself;

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

        for (String s : keyArray) {
            String friendJson = (String) store.get(s);
            Friend f = Friend.fromString(friendJson);
            if ("yourself".equals(s)) {
                yourself = f;
            } else {
                storedFriends.add(f);
            }
        }
    }



    public boolean isItYourself(Friend friend) {
        return friend.getId() == yourself.getId();
    }



    @Override
    public void onFirstAppStart(List<Friend> friendList, Friend yourself) {
        saveFriends(friendList);

        // saving yourself
        saveOrUpdateYourself(yourself);

        backendService.onFirstAppStart(friendList);
    }



    @Override
    public void onAppStart(List<Friend> friendList, Friend yourself) {
        saveOrUpdateYourself(yourself);

        List<Friend> knownFriends = new ArrayList<>(friendList.size());
        knownFriends.addAll(friendList);

        // checking for changes at old friends
        for (Friend f1 : knownFriends) {
            for (Friend f2 : storedFriends) {
                checkFriendChange(f1, f2);
            }
        }

        // deleted friends
        List<Friend> deletedFriends = new ArrayList<>();
        deletedFriends.addAll(storedFriends);
        deletedFriends.removeAll(friendList);

        // new friends
        friendList.removeAll(storedFriends);

        // handling new friends
        for (Friend f : friendList) {
            new UpdateFriendsImageTask(f).execute();
        }

        // deleting friends
        deleteFriends(deletedFriends);

        editor.apply();
        backendService.onAppStart();
    }




    public void saveOrUpdateYourself(Friend yourself) {
        if (yourself != null) {
            if (this.yourself == null) {
                this.yourself = yourself;
                editor.putString("yourself", yourself.toString());
                editor.apply();
                new UpdateFriendsImageTask(yourself).execute();

            } else {
                checkFriendChange(yourself, this.yourself);
            }
        }
    }




    private void checkFriendChange(Friend newFriendState, Friend oldFriendState) {
        // if they are the same
        if (newFriendState.getId() == oldFriendState.getId()) {
            // if his image is changed
            if (!newFriendState.getImageUrl().equals(oldFriendState.getImageUrl())) {
                new UpdateFriendsImageTask(newFriendState).execute();
            }

            // if his name is changed
            if (!newFriendState.getName().equals(oldFriendState.getName())) {
                updateFriendOrYourself(newFriendState);
            }
        }
    }


    public void updateFriendOrYourself(Friend friend) {
        if (friend.getId() != yourself.getId()) {
            Friend previous = findFriendById(friend.getId());
            storedFriends.remove(previous);
            storedFriends.add(friend);
            editor.putString(Long.toString(friend.getId()), friend.toString());
            editor.apply();
            if (friendChangeListener != null) {
                friendChangeListener.onFriendsChanged();
            }

        } else {
            yourself = friend;
            editor.putString("yourself", yourself.toString());
            editor.apply();
        }
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
            FriendsManager.getInstance().updateFriendOrYourself(friend);
        }
    }





    public Friend findFriendById(long friendId) {
        Friend returnFriend = null;
        for (Friend friend : storedFriends) {
            if (friend.getId() == friendId) {
                returnFriend = friend;
                break;
            }
        }

        if (returnFriend == null) {
            if (friendId == yourself.getId()) {
                returnFriend = yourself;
            }
        }

        return returnFriend;
    }





    private void saveFriends(List<Friend> toBeSaved) {
        for (Friend f : toBeSaved) {
            editor.putString(Long.toString(f.getId()), f.toString());
            storedFriends.add(f);
            new UpdateFriendsImageTask(f).execute();
        }
        editor.apply();
    }


    @Override
    public void onNoInternetStart() {
    }


    private void deleteFriends(List<Friend> toBeDeleted) {
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

    public Friend getYourself() {
        return yourself;
    }
}
