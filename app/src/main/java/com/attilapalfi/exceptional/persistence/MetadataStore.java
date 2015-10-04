package com.attilapalfi.exceptional.persistence;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener;
import com.attilapalfi.exceptional.interfaces.PointChangeListener;
import com.attilapalfi.exceptional.model.Friend;
import io.paperdb.Book;
import io.paperdb.Paper;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by palfi on 2015-08-21.
 */
public class MetadataStore {
    private static final String METADATA_DATABASE = "METADATA_DATABASE";
    private static final String EXCEPTION_VERSION = "exceptionVersion";
    private static final String LOGGED_IN = "loggedIn";
    private static final String FIRST_START_FINISHED = "firstStartFinished";
    private static final String VOTED_THIS_WEEK = "votedThisWeek";
    private static final String SUBMITTED_THIS_WEEK = "submittedThisWeek";
    private static final String USER = "user";
    private static final Friend EMPTY_USER = new Friend( new BigInteger( "0" ), "", "", "", 100, false );

    @Inject ImageCache imageCache;
    private Book database;
    private Handler handler;
    private volatile int exceptionVersion = 0;
    private volatile boolean loggedIn = false;
    private volatile boolean firstStartFinished = false;
    private volatile boolean votedThisWeek = true;
    private volatile boolean submittedThisWeek = true;
    private volatile Friend user = EMPTY_USER;
    private Set<FirstStartFinishedListener> firstStartFinishedListeners = new HashSet<>();
    private Set<PointChangeListener> pointChangeListeners = new HashSet<>();

    public MetadataStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        database = Paper.book( METADATA_DATABASE );
        handler = new Handler( Looper.getMainLooper() );
        initMetadata();
    }

    private void initMetadata( ) {
        exceptionVersion = database.read( EXCEPTION_VERSION, exceptionVersion );
        loggedIn = database.read( LOGGED_IN, loggedIn );
        firstStartFinished = database.read( FIRST_START_FINISHED, firstStartFinished );
        votedThisWeek = database.read( VOTED_THIS_WEEK, votedThisWeek );
        submittedThisWeek = database.read( SUBMITTED_THIS_WEEK, submittedThisWeek );
        user = database.read( USER, EMPTY_USER );
    }

    public void setPoints( int points ) {
        if ( user.getPoints() != points ) {
            user.setPoints( points );;
            database.write( USER, user );
            if ( Looper.myLooper() == Looper.getMainLooper() ) {
                stream( pointChangeListeners ).forEach( PointChangeListener::onPointsChanged );
            } else {
                handler.post( () -> stream( pointChangeListeners )
                        .forEach( PointChangeListener::onPointsChanged ) );
            }
        }
    }

    public int getPoints( ) {
        return user.getPoints();
    }

    public void setExceptionVersion( int exceptionVersion ) {
        if ( this.exceptionVersion != exceptionVersion ) {
            this.exceptionVersion = exceptionVersion;
            database.write( EXCEPTION_VERSION, exceptionVersion );
        }
    }

    public int getExceptionVersion( ) {
        return exceptionVersion;
    }

    public void setFirstStartFinished( boolean firstStartFinished ) {
        if ( this.firstStartFinished != firstStartFinished ) {
            this.firstStartFinished = firstStartFinished;
            database.write( FIRST_START_FINISHED, firstStartFinished );
        }
        for ( FirstStartFinishedListener l : firstStartFinishedListeners ) {
            l.onFirstStartFinished( firstStartFinished );
        }
    }

    public boolean isFirstStartFinished( ) {
        return firstStartFinished;
    }

    public void setLoggedIn( boolean loggedIn ) {
        if ( this.loggedIn != loggedIn ) {
            this.loggedIn = loggedIn;
            database.write( LOGGED_IN, loggedIn );
        }
    }

    public boolean isLoggedIn( ) {
        return loggedIn;
    }

    public void setVotedThisWeek( boolean votedThisWeek ) {
        if ( this.votedThisWeek != votedThisWeek ) {
            this.votedThisWeek = votedThisWeek;
            database.write( VOTED_THIS_WEEK, votedThisWeek );
        }
    }

    public boolean isVotedThisWeek( ) {
        return votedThisWeek;
    }

    public void setSubmittedThisWeek( boolean submittedThisWeek ) {
        if ( this.submittedThisWeek != submittedThisWeek ) {
            this.submittedThisWeek = submittedThisWeek;
            database.write( SUBMITTED_THIS_WEEK, submittedThisWeek );
        }
    }

    public boolean isSubmittedThisWeek( ) {
        return submittedThisWeek;
    }


    public boolean isItUser( Friend friend ) {
        return friend.equals( user );
    }

    public Friend getUser( ) {
        return user;
    }

    private void saveUser( Friend user ) {
        user.setImageLoaded( true );
        this.user = user;
        database.write( USER, user );
    }


    public void updateUser( Friend newUserState ) {
        newUserState.setImageLoaded( true );
        lookForChange( newUserState );
    }

    private void lookForChange( Friend newUserState ) {
        boolean changed = false;
        if ( !newUserState.getImageUrl().equals( user.getImageUrl() ) ) {
            changed = true;
            imageCache.updateImageAsync( newUserState, user );
        }
        if ( !newUserState.getName().equals( user.getName() ) ) {
            changed = true;
        }
        if ( changed ) {
            saveUser( newUserState );
        }
    }

    public void wipe( ) {
        exceptionVersion = 0;
        loggedIn = false;
        firstStartFinished = false;
        database.destroy();
    }

    public boolean addFirstStartFinishedListener( FirstStartFinishedListener listener ) {
        return firstStartFinishedListeners.add( listener );
    }

    public boolean removeFirstStartFinishedListener( FirstStartFinishedListener listener ) {
        return firstStartFinishedListeners.remove( listener );
    }

    public boolean addPointChangeListener( PointChangeListener listener ) {
        return pointChangeListeners.add( listener );
    }

    public boolean removePointChangeListener( PointChangeListener listener ) {
        return pointChangeListeners.remove( listener );
    }
}
