package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendsManager implements Wipeable {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final List<Friend> storedFriends = new LinkedList<>();
    private Friend yourself;
    private Set<FriendChangeListener> friendChangeListeners = new HashSet<>();
    private static FriendsManager instance;

    public static FriendsManager getInstance() {
        if (instance == null) {
            instance = new FriendsManager();
        }
        return instance;
    }

    public void addFriendChangeListener(FriendChangeListener listener) {
        friendChangeListeners.add(listener);
    }

    public void removeFriendChangeListener(FriendChangeListener listener) {
        friendChangeListeners.remove(listener);
    }

    public List<Friend> getStoredFriends() {
        return storedFriends;
    }

    public Friend getYourself() {
        return yourself;
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
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> keys = store.keySet();
        for (String s : keys) {
            String friendJson = (String) store.get(s);
            Friend f = Friend.fromString(friendJson);
            if ("yourself".equals(s)) {
                yourself = f;
            } else {
                storedFriends.add(f);
            }
        }
        editor.apply();
        notifyChangeListeners();
    }

    public void saveFriendsAndYourself(List<Friend> friendList, Friend yourself) {
        saveFriends(friendList);
        saveOrUpdateYourself(yourself);
    }

    public void updateFriendsAndYourself(List<Friend> friendList, Friend yourself) {
        saveOrUpdateYourself(yourself);
        updateOldFriends(friendList);
        removeDeletedFriends(friendList);
        saveNewFriends(friendList);
    }

    public void updateFriendPoints(BigInteger id, int points) {
        Friend friend = findFriendById(id);
        if (friend.getPoints() != points) {
            friend.setPoints(points);
            updateFriend(friend);
        }
    }

    public void updateFriendsPoints(Map<BigInteger, Integer> points) {
        for (BigInteger facebookId : points.keySet()) {
            Friend friend = findFriendById(facebookId);
            if (friend.getPoints() != points.get(facebookId)) {
                friend.setPoints(points.get(facebookId));
                updateFriend(friend);
            }
        }
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

    @Override
    public void wipe() {
        storedFriends.clear();
        editor.clear();
        editor.apply();
        notifyChangeListeners();
    }

    public boolean isItYourself(Friend friend) {
        return areTheyTheSamePerson(friend, yourself);
    }

    public Friend findFriendById(BigInteger friendId) {
        Friend friend = tryFindFriendInStore(friendId);
        if (friend != null)
            return friend;
        else {
            if (yourself != null) {
                if (friendId.equals(yourself.getId())) {
                    return yourself;
                }
            }
        }
        return new Friend(new BigInteger("0"), "", "", "");
    }

    private void saveFriends(List<Friend> toBeSaved) {
        for (Friend f : toBeSaved) {
            editor.putString(f.getId().toString(), f.toString());
            storedFriends.add(f);
            new UpdateFriendsImageTask(f).execute();
        }
        editor.apply();
        notifyChangeListeners();
    }

    private void updateOldFriends(List<Friend> friendList) {
        List<Friend> knownCurrentFriends = new ArrayList<>(friendList.size());
        knownCurrentFriends.addAll(friendList);
        // checking for changes at old friends
        for (Friend f1 : knownCurrentFriends) {
            for (Friend f2 : storedFriends) {
                checkFriendChange(f1, f2);
            }
        }
    }

    private void removeDeletedFriends(List<Friend> friendList) {
        List<Friend> deletedFriends = new ArrayList<>();
        deletedFriends.addAll(storedFriends);
        deletedFriends.removeAll(friendList);
        deleteFriends(deletedFriends);
    }

    private void saveNewFriends(List<Friend> friendList) {
        List<Friend> workList = new ArrayList<>(friendList);
        workList.removeAll(storedFriends);
        for (Friend f : workList) {
            new UpdateFriendsImageTask(f).execute();
        }
    }

    private void checkFriendChange(Friend newFriendState, Friend oldFriendState) {
        if (areTheyTheSamePerson(newFriendState, oldFriendState)) {
            if (isImageChanged(newFriendState, oldFriendState)) {
                new UpdateFriendsImageTask(newFriendState).execute();
            }
            if (isNameChanged(newFriendState, oldFriendState)) {
                updateFriendOrYourself(newFriendState);
            }
        }
    }

    private boolean areTheyTheSamePerson(Friend newFriendState, Friend oldFriendState) {
        return newFriendState.getId().equals(oldFriendState.getId());
    }

    private boolean isImageChanged(Friend newFriendState, Friend oldFriendState) {
        return !newFriendState.getImageUrl().equals(oldFriendState.getImageUrl());
    }

    private boolean isNameChanged(Friend newFriendState, Friend oldFriendState) {
        return (!newFriendState.getFirstName().equals(oldFriendState.getFirstName()) ||
                !newFriendState.getLastName().equals(oldFriendState.getLastName()));
    }

    private void updateFriendOrYourself(Friend someone) {
        if (!areTheyTheSamePerson(someone, yourself)) {
            updateFriend(someone);
        } else {
            updateYourself(someone);
        }
    }

    private Friend tryFindFriendInStore(BigInteger friendId) {
        for (Friend friend : storedFriends) {
            if (friend.getId().equals(friendId)) {
                return friend;
            }
        }
        return null;
    }

    private void updateFriend(Friend someone) {
        Friend previous = findFriendById(someone.getId());
        storedFriends.remove(previous);
        storedFriends.add(someone);
        editor.putString(someone.getId().toString(), someone.toString());
        editor.apply();
        notifyChangeListeners();
    }

    private void updateYourself(Friend someone) {
        yourself = someone;
        editor.putString("yourself", yourself.toString());
        editor.apply();
        notifyChangeListeners();
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
                    URLConnection connection = createUrlConnection(downloadUrl);
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

        @NonNull
        private URLConnection createUrlConnection(String downloadUrl) throws IOException {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.setUseCaches(true);
            return connection;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            friend.setImage(bitmap);
            FriendsManager.getInstance().updateFriendOrYourself(friend);
        }

    }
    private void deleteFriends(List<Friend> toBeDeleted) {
        for (Friend f : toBeDeleted) {
            editor.remove((f.getId().toString()));
        }
        editor.apply();
        storedFriends.removeAll(toBeDeleted);
        notifyChangeListeners();
    }

    private void notifyChangeListeners() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (FriendChangeListener listener : friendChangeListeners) {
                listener.onFriendsChanged();
            }
        }
    }
}
