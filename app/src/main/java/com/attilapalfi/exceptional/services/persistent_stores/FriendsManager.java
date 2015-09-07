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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.annimon.stream.Stream.of;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendsManager {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static final List<Friend> storedFriends = Collections.synchronizedList(new LinkedList<>());
    private static Friend yourself;
    private static Set<FriendChangeListener> friendChangeListeners = new HashSet<>();

    public static void addFriendChangeListener(FriendChangeListener listener) {
        friendChangeListeners.add(listener);
    }

    public static void removeFriendChangeListener(FriendChangeListener listener) {
        friendChangeListeners.remove(listener);
    }

    public static List<Friend> getStoredFriends() {
        return storedFriends;
    }

    public static Friend getYourself() {
        return yourself;
    }

    private FriendsManager() {
    }

    public static boolean isInitialized() {
        return sharedPreferences != null;
    }

    public static void initialize(Context context) {
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
        new AsyncFriendOrganizer().execute();
        notifyChangeListeners();
    }

    public static void saveFriendsAndYourself(List<Friend> friendList, Friend yourself) {
        saveFriends(friendList);
        saveOrUpdateYourself(yourself);
    }

    public static void updateFriendsAndYourself(List<Friend> friendList, Friend yourself) {
        saveOrUpdateYourself(yourself);
        updateOldFriends(friendList);
        removeDeletedFriends(friendList);
        saveNewFriends(friendList);
    }

    public static void updateFriendPoints(BigInteger id, int points) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                findAndUpdateFriend(id, points);
                Collections.sort(storedFriends, new Friend.PointComparator());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyChangeListeners();
            }

        }.execute();
    }

    private static void findAndUpdateFriend(BigInteger id, int points) {
        Friend friend = findFriendById(id);
        if (friend.getPoints() != points) {
            friend.setPoints(points);
            updateFriend(friend);
        }
    }

    public static void updateFriendsPoints(Map<BigInteger, Integer> points) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                of(points.keySet()).forEach(facebookId -> findAndUpdateFriend(facebookId, points.get(facebookId)));
                Collections.sort(storedFriends, new Friend.PointComparator());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                notifyChangeListeners();
            }

        }.execute();
    }

    public static void saveOrUpdateYourself(Friend yourself) {
        if (yourself != null) {
            if (FriendsManager.yourself == null) {
                FriendsManager.yourself = yourself;
                editor.putString("yourself", yourself.toString());
                editor.apply();
                new UpdateFriendsImageTask(yourself).execute();

            } else {
                checkFriendChange(yourself, FriendsManager.yourself);
            }
        }
    }

    public static void wipe() {
        storedFriends.clear();
        editor.clear();
        editor.apply();
        notifyChangeListeners();
    }

    public static boolean isItYourself(Friend friend) {
        return areTheyTheSamePerson(friend, yourself);
    }

    public static Friend findFriendById(BigInteger friendId) {
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

    private static void saveFriends(List<Friend> toBeSaved) {
        for (Friend f : toBeSaved) {
            editor.putString(f.getId().toString(), f.toString());
            storedFriends.add(f);
            new UpdateFriendsImageTask(f).execute();
        }
        editor.apply();
        notifyChangeListeners();
    }

    private static void updateOldFriends(List<Friend> friendList) {
        List<Friend> knownCurrentFriends = new ArrayList<>(friendList.size());
        knownCurrentFriends.addAll(friendList);
        // checking for changes at old friends
        for (Friend f1 : knownCurrentFriends) {
            for (Friend f2 : storedFriends) {
                checkFriendChange(f1, f2);
            }
        }
    }

    private static void removeDeletedFriends(List<Friend> friendList) {
        List<Friend> deletedFriends = new ArrayList<>();
        deletedFriends.addAll(storedFriends);
        deletedFriends.removeAll(friendList);
        deleteFriends(deletedFriends);
    }

    private static void saveNewFriends(List<Friend> friendList) {
        List<Friend> workList = new ArrayList<>(friendList);
        workList.removeAll(storedFriends);
        for (Friend f : workList) {
            new UpdateFriendsImageTask(f).execute();
        }
    }

    private static void checkFriendChange(Friend newFriendState, Friend oldFriendState) {
        if (areTheyTheSamePerson(newFriendState, oldFriendState)) {
            if (isImageChanged(newFriendState, oldFriendState)) {
                new UpdateFriendsImageTask(newFriendState).execute();
            }
            if (isNameChanged(newFriendState, oldFriendState)) {
                updateFriendOrYourself(newFriendState);
            }
        }
    }

    private static boolean areTheyTheSamePerson(Friend newFriendState, Friend oldFriendState) {
        return newFriendState.getId().equals(oldFriendState.getId());
    }

    private static boolean isImageChanged(Friend newFriendState, Friend oldFriendState) {
        return !newFriendState.getImageUrl().equals(oldFriendState.getImageUrl());
    }

    private static boolean isNameChanged(Friend newFriendState, Friend oldFriendState) {
        return (!newFriendState.getFirstName().equals(oldFriendState.getFirstName()) ||
                !newFriendState.getLastName().equals(oldFriendState.getLastName()));
    }

    private static void updateFriendOrYourself(Friend someone) {
        if (!areTheyTheSamePerson(someone, yourself)) {
            updateFriend(someone);
            new AsyncFriendOrganizer().execute();
            notifyChangeListeners();
        } else {
            updateYourself(someone);
        }
    }

    private static Friend tryFindFriendInStore(BigInteger friendId) {
        for (Friend friend : storedFriends) {
            if (friend.getId().equals(friendId)) {
                return friend;
            }
        }
        return null;
    }

    private static void updateFriend(Friend newOne) {
        Friend previousOne = findFriendById(newOne.getId());
        storedFriends.remove(previousOne);
        storedFriends.add(newOne);
        editor.putString(newOne.getId().toString(), newOne.toString());
        editor.apply();
    }

    private static void updateYourself(Friend someone) {
        yourself = someone;
        editor.putString("yourself", yourself.toString());
        editor.apply();
        notifyChangeListeners();
    }

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
                friendPicture = ImageCache.getImageForFriend(friend);
                if (friendPicture == null) {
                    friendPicture = downloadAndDecodeBitmap(downloadUrl, friendPicture);
                    ImageCache.addImage(friend, friendPicture);
                }
            } catch (IOException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return friendPicture;
        }

        private Bitmap downloadAndDecodeBitmap(String downloadUrl, Bitmap friendPicture) throws IOException {
            URLConnection connection = createUrlConnection(downloadUrl);
            InputStream inputStream = (InputStream) connection.getContent();
            friendPicture = BitmapFactory.decodeStream(inputStream);
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
            FriendsManager.updateFriendOrYourself(friend);
        }

    }
    private static void deleteFriends(List<Friend> toBeDeleted) {
        for (Friend f : toBeDeleted) {
            editor.remove((f.getId().toString()));
        }
        editor.apply();
        storedFriends.removeAll(toBeDeleted);
        notifyChangeListeners();
    }

    private static void notifyChangeListeners() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            of(friendChangeListeners).forEach(FriendChangeListener::onFriendsChanged);
        }
    }

    private static class AsyncFriendOrganizer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Collections.sort(storedFriends, new Friend.PointComparator());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            notifyChangeListeners();
        }
    }
}
