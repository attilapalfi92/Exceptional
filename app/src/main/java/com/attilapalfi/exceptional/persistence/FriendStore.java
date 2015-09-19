package com.attilapalfi.exceptional.persistence;

import java.math.BigInteger;
import java.util.*;

import javax.inject.Inject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.model.*;
import io.paperdb.Book;
import io.paperdb.Paper;

import static java8.util.stream.StreamSupport.stream;

/**
 * Responsible for storing friends in the device's preferences
 * Created by Attila on 2015-06-12.
 */
public class FriendStore {
    private static final String FRIEND_DATABASE = "FRIEND_DATABASE";
    private static final String FRIEND_IDS = "FRIEND_IDS";
    private static final Friend EMPTY_FRIEND = new Friend( new BigInteger( "0" ), "", "", "" );

    @Inject ImageCache imageCache;
    private final Book database;
    private Handler handler;
    private final List<Friend> storedFriends = Collections.synchronizedList( new LinkedList<>() );
    private List<BigInteger> idList;
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
        database = Paper.book( FRIEND_DATABASE );
        handler = new Handler( Looper.getMainLooper() );
        idList = Collections.synchronizedList( database.read( FRIEND_IDS, new LinkedList<>() ) );
        stream( idList ).forEach( id -> ( storedFriends ).add( database.read( id.toString(), EMPTY_FRIEND ) ) );
        new AsyncFriendOrganizer().execute();
    }

    public void saveFriendList( List<Friend> friendList ) {
        storedFriends.addAll( friendList );
        imageCache.loadImagesInitially( friendList );
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                stream( friendList ).forEach( friend -> {
                    idList.add( friend.getId() );
                    database.write( friend.getId().toString(), friend );
                } );
                database.write( FRIEND_IDS, idList );
                Collections.sort( storedFriends );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    public void updateFriendList( List<Friend> friendList ) {
        saveNewFriends( friendList );
        updateOldFriends( friendList );
        removeDeletedFriends( friendList );
    }

    public void updateFriendPoints( BigInteger id, int points ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                updatePointsById( id, points );
                Collections.sort( storedFriends );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    public void updatePointsOfFriends( Map<BigInteger, Integer> points ) {
        stream( points.keySet() ).forEach( id -> updatePointsById( id, points.get( id ) ) );
        Collections.sort( storedFriends );
        handler.post( this::notifyChangeListeners );
    }

    public void updatePointsOfFriendsAsync( Map<BigInteger, Integer> points ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                stream( points.keySet() ).forEach( id -> updatePointsById( id, points.get( id ) ) );
                Collections.sort( storedFriends );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    private void updatePointsById( BigInteger id, int point ) {
        Friend friend = findFriendById( id );
        if ( point != friend.getPoints() ) {
            friend.setPoints( point );
            database.write( id.toString(), friend );
        }
    }

    public Friend findFriendById( BigInteger friendId ) {
        for ( Friend friend : storedFriends ) {
            if ( friend.getId().equals( friendId ) ) {
                return friend;
            }
        }
        return new Friend( new BigInteger( "0" ), "", "", "" );
    }

    public void wipe( ) {
        storedFriends.clear();
        idList.clear();
        database.destroy();
        notifyChangeListeners();
    }

    private void saveNewFriends( List<Friend> friendList ) {
        List<Friend> workList = new ArrayList<>( friendList );
        workList.removeAll( storedFriends );
        saveFriendList( workList );
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

    private void checkFriendChange( Friend newFriendState, Friend oldFriendState ) {
        if ( newFriendState.equals( oldFriendState ) ) {
            if ( !newFriendState.getImageUrl().equals( oldFriendState.getImageUrl() ) ) {
                updateFriend( newFriendState, oldFriendState );
                imageCache.updateImageAsync( newFriendState, oldFriendState );
            }
            if ( !newFriendState.getName().equals( oldFriendState.getName() ) ) {
                updateFriend( newFriendState, oldFriendState );
            }
        }
    }

    private void updateFriend( Friend newState, Friend oldInstance ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                oldInstance.setFirstName( newState.getFirstName() );
                oldInstance.setLastName( newState.getLastName() );
                oldInstance.setImageUrl( newState.getImageUrl() );
                database.write( oldInstance.getId().toString(), oldInstance );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();

    }

    private void deleteFriends( List<Friend> toBeDeleted ) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                stream( toBeDeleted ).forEach( friend -> {
                    storedFriends.remove( friend );
                    database.delete( friend.getId().toString() );
                    idList.remove( friend.getId() );
                } );
                database.write( FRIEND_IDS, idList );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }

    private void notifyChangeListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            stream( friendChangeListeners ).forEach( FriendChangeListener::onFriendsChanged );
        }
    }

    private class AsyncFriendOrganizer extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground( Void... params ) {
            Collections.sort( storedFriends );
            return null;
        }

        @Override
        protected void onPostExecute( Void aVoid ) {
            notifyChangeListeners();
        }

    }
}
