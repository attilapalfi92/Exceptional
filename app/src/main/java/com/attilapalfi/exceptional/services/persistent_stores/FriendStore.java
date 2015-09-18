package com.attilapalfi.exceptional.services.persistent_stores;

import java.math.BigInteger;
import java.util.*;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.model.Friend;

import static java8.util.stream.StreamSupport.stream;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendStore {
    @Inject Context context;
    @Inject ImageCache imageCache;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final List<Friend> storedFriends = Collections.synchronizedList( new LinkedList<>() );
    private Set<FriendChangeListener> friendChangeListeners = new HashSet<>();

    public void addFriendChangeListener( FriendChangeListener listener ) {
        friendChangeListeners.add( listener );
    }

    public void removeFriendChangeListener( FriendChangeListener listener ) {
        friendChangeListeners.remove( listener );
    }

    public List<Friend> getStoredFriends( ) {
        return storedFriends;
    }

    public FriendStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        String PREFS_NAME = context.getString( R.string.friends_preferences );
        sharedPreferences = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
        editor = sharedPreferences.edit();
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> keys = store.keySet();
        for ( String s : keys ) {
            String friendJson = (String) store.get( s );
            Friend f = Friend.fromString( friendJson );
            storedFriends.add( f );
        }
        editor.apply();
        new AsyncFriendOrganizer().execute();
        notifyChangeListeners();
    }

    public void saveFriendList( List<Friend> friendList ) {
        for ( Friend f : friendList ) {
            editor.putString( f.getId().toString(), f.toString() );
            storedFriends.add( f );
        }
        editor.apply();
        notifyChangeListeners();
    }

    public void updateFriendList( List<Friend> friendList ) {
        updateOldFriends( friendList );
        removeDeletedFriends( friendList );
        saveNewFriends( friendList );
    }

    public void updateFriendPoints( BigInteger id, int points ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                findAndUpdateFriend( id, points );
                Collections.sort( storedFriends, new Friend.PointComparator() );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    private void findAndUpdateFriend( BigInteger id, int points ) {
        Friend friend = findFriendById( id );
        if ( friend.getPoints() != points ) {
            friend.setPoints( points );
            updateFriend( friend );
        }
    }

    public void updateFriendsPoints( Map<BigInteger, Integer> points ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                stream( points.keySet() ).forEach( facebookId -> findAndUpdateFriend( facebookId, points.get( facebookId ) ) );
                Collections.sort( storedFriends, new Friend.PointComparator() );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    public void wipe( ) {
        storedFriends.clear();
        editor.clear();
        editor.apply();
        notifyChangeListeners();
    }

    public Friend findFriendById( BigInteger friendId ) {
        for ( Friend friend : storedFriends ) {
            if ( friend.getId().equals( friendId ) ) {
                return friend;
            }
        }
        return new Friend( new BigInteger( "0" ), "", "", "" );
    }

    private void updateOldFriends( List<Friend> friendList ) {
        List<Friend> knownCurrentFriends = new ArrayList<>( friendList.size() );
        knownCurrentFriends.addAll( friendList );
        for ( Friend f1 : knownCurrentFriends ) {
            for ( Friend f2 : storedFriends ) {
                checkFriendChange( f1, f2 );
            }
        }
    }

    private void removeDeletedFriends( List<Friend> friendList ) {
        List<Friend> deletedFriends = new ArrayList<>();
        deletedFriends.addAll( storedFriends );
        deletedFriends.removeAll( friendList );
        deleteFriends( deletedFriends );
    }

    private void saveNewFriends( List<Friend> friendList ) {
        List<Friend> workList = new ArrayList<>( friendList );
        workList.removeAll( storedFriends );
        saveFriendList( workList );
    }

    private void checkFriendChange( Friend newFriendState, Friend oldFriendState ) {
        if ( newFriendState.equals( oldFriendState ) ) {
            if ( !newFriendState.getImageUrl().equals( oldFriendState.getImageUrl() ) ) {
                updateFriend( newFriendState );
                imageCache.updateImageAsync( newFriendState, oldFriendState );
            }
            if ( !newFriendState.getName().equals( oldFriendState.getName() ) ) {
                updateFriend( newFriendState );
            }
        }
    }

    private void updateFriend( Friend newOne ) {
        Friend previousOne = findFriendById( newOne.getId() );
        storedFriends.remove( previousOne );
        storedFriends.add( newOne );
        editor.putString( newOne.getId().toString(), newOne.toString() );
        editor.apply();
        new AsyncFriendOrganizer().execute();
        notifyChangeListeners();
    }

    private void deleteFriends( List<Friend> toBeDeleted ) {
        for ( Friend f : toBeDeleted ) {
            editor.remove( ( f.getId().toString() ) );
        }
        editor.apply();
        storedFriends.removeAll( toBeDeleted );
        notifyChangeListeners();
    }

    private void notifyChangeListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            stream( friendChangeListeners ).forEach( FriendChangeListener::onFriendsChanged );
        }
    }

    private class AsyncFriendOrganizer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground( Void... params ) {
            Collections.sort( storedFriends, new Friend.PointComparator() );
            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid ) {
            notifyChangeListeners();
        }
    }
}
