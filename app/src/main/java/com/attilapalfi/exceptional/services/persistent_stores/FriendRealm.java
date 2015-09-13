package com.attilapalfi.exceptional.services.persistent_stores;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.inject.Inject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.model.Friend;
import io.realm.Realm;
import io.realm.RealmResults;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by palfi on 2015-09-13.
 */
public class FriendRealm {
    @Inject
    Context context;
    @Inject
    ImageCache imageCache;
    private Set<FriendChangeListener> friendChangeListeners = new HashSet<>();

    public FriendRealm( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        notifyChangeListeners();
    }

    public void addFriendChangeListener( FriendChangeListener listener ) {
        friendChangeListeners.add( listener );
    }

    public void removeFriendChangeListener( FriendChangeListener listener ) {
        friendChangeListeners.remove( listener );
    }

    public void saveFriends( List<Friend> toBeSaved ) {
        try ( Realm realm = Realm.getInstance( context ) ) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate( toBeSaved );
            realm.commitTransaction();
        }
        imageCache.downloadAllFriendsImage();
        notifyChangeListeners();
    }

    public void updateFriends( List<Friend> friendList ) {
        updateOldFriends( friendList );
        removeDeletedFriends( friendList );
        saveNewFriends( friendList );
    }

    public void updateFriendPoints( String id, int points ) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( Void... params ) {
                try ( Realm realm = Realm.getInstance( context ) ) {
                    realm.beginTransaction();
                    Friend friend = realm.where( Friend.class ).equalTo( "id", id ).findFirst();
                    friend.setPoints( points );
                    realm.commitTransaction();
                    return null;
                }
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }

        }.execute();
    }


    public void updatePointsOfFriendList( Map<String, Integer> points ) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( Void... params ) {
                try ( Realm realm = Realm.getInstance( context ) ) {
                    realm.executeTransaction( realm1 -> {
                        List<Friend> friends = realm1.allObjects( Friend.class );
                        for ( Friend friend : friends ) {
                            if ( points.containsKey( friend.getId() ) ) {
                                friend.setPoints( points.get( friend.getId() ) );
                            }
                        }
                    } );
                    return null;
                }
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyChangeListeners();
            }
        }.execute();
    }

    public void wipe( ) {
        try ( Realm realm = Realm.getInstance( context ) ) {
            realm.beginTransaction();
            realm.clear( Friend.class );
            realm.commitTransaction();
            notifyChangeListeners();
        }
    }

    private void updateOldFriends( List<Friend> friendList ) {
        List<Friend> knownCurrentFriends = new ArrayList<>( friendList.size() );
        knownCurrentFriends.addAll( friendList );
        try ( Realm realm = Realm.getInstance( context ) ) {
            RealmResults<Friend> storedFriends = realm.allObjects( Friend.class );
            for ( Friend f1 : knownCurrentFriends ) {
                for ( Friend f2 : storedFriends ) {
                    checkFriendChange( f1, f2 );
                }
            }
        }
    }

    private void removeDeletedFriends( List<Friend> friendList ) {
        List<Friend> deletedFriends = new ArrayList<>();
        try ( Realm realm = Realm.getInstance( context ) ) {
            deletedFriends.addAll( realm.allObjects( Friend.class ) );
        }
        deletedFriends.removeAll( friendList );
        deleteFriends( deletedFriends );
    }

    private void saveNewFriends( List<Friend> friendList ) {
        List<Friend> workList = new ArrayList<>( friendList );
        try ( Realm realm = Realm.getInstance( context ) ) {
            realm.beginTransaction();
            workList.removeAll( realm.allObjects( Friend.class ) );
            realm.copyToRealmOrUpdate( workList );
            realm.commitTransaction();
        }
        /*for ( Friend f : workList ) {
            new UpdateFriendsImageTask( f.getId() ).execute();
        }*/
    }

    private void checkFriendChange( Friend newFriendState, Friend oldFriendState ) {
        if ( areTheyTheSamePerson( newFriendState, oldFriendState ) ) {
            if ( isImageChanged( newFriendState, oldFriendState ) ) {
                try ( Realm realm = Realm.getInstance( context ) ) {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate( newFriendState );
                    realm.commitTransaction();
                    new UpdateFriendsImageTask( newFriendState.getId() ).execute();
                }
            }
            if ( isNameChanged( newFriendState, oldFriendState ) ) {
                updateFriend( newFriendState );
            }
        }
    }

    private boolean areTheyTheSamePerson( Friend newFriendState, Friend oldFriendState ) {
        return newFriendState.getId().equals( oldFriendState.getId() );
    }

    private boolean isImageChanged( Friend newFriendState, Friend oldFriendState ) {
        return !newFriendState.getImageUrl().equals( oldFriendState.getImageUrl() );
    }

    private boolean isNameChanged( Friend newFriendState, Friend oldFriendState ) {
        return ( !newFriendState.getFirstName().equals( oldFriendState.getFirstName() ) ||
                !newFriendState.getLastName().equals( oldFriendState.getLastName() ) );
    }

    private void updateFriend( Friend newOne ) {
        try ( Realm realm = Realm.getInstance( context ) ) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate( newOne );
            realm.commitTransaction();
            notifyChangeListeners();
        }
    }

    private class UpdateFriendsImageTask extends AsyncTask<Void, Void, Bitmap> {
        String id;

        public UpdateFriendsImageTask( String id ) {
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground( Void... param ) {
            try ( Realm realm = Realm.getInstance( context ) ) {
                Friend friend = realm.where( Friend.class ).equalTo( "id", id ).findFirst();
                String downloadUrl = friend.getImageUrl();
                Bitmap friendPicture = null;
                try {
                    friendPicture = imageCache.getImageForFriend( friend );
                    if ( friendPicture == null ) {
                        friendPicture = downloadAndDecodeBitmap( downloadUrl );
                        imageCache.saveImageToStore( friend, friendPicture );
                    }
                } catch ( IOException e ) {
                    Log.e( "Error", e.getMessage() );
                    e.printStackTrace();
                }
                return friendPicture;
            }
        }

        private Bitmap downloadAndDecodeBitmap( String downloadUrl ) throws IOException {
            URLConnection connection = createUrlConnection( downloadUrl );
            InputStream inputStream = (InputStream) connection.getContent();
            return BitmapFactory.decodeStream( inputStream );
        }

        @NonNull
        private URLConnection createUrlConnection( String downloadUrl ) throws IOException {
            URL url = new URL( downloadUrl );
            URLConnection connection = url.openConnection();
            connection.setUseCaches( true );
            return connection;
        }

        @Override
        protected void onPostExecute( Bitmap bitmap ) {
            try ( Realm realm = Realm.getInstance( context ) ) {
                realm.beginTransaction();
                Friend friend = realm.where( Friend.class ).equalTo( "id", id ).findFirst();
                friend.setImageWeakReference( new WeakReference<>( bitmap ) );
                realm.commitTransaction();

                updateFriend( friend );
            }
        }

    }

    private void deleteFriends( List<Friend> toBeDeleted ) {
        try ( Realm realm = Realm.getInstance( context ) ) {
            realm.beginTransaction();
            stream( toBeDeleted ).map( Friend::getId ).forEach( id -> {
                realm.where( Friend.class ).equalTo( "id", id ).findFirst().removeFromRealm();
            } );
            realm.commitTransaction();
            notifyChangeListeners();
        }
    }

    private void notifyChangeListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            stream( friendChangeListeners ).forEach( FriendChangeListener::onFriendsChanged );
        }
    }
}
