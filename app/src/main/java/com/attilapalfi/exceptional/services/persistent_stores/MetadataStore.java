package com.attilapalfi.exceptional.services.persistent_stores;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener;
import com.attilapalfi.exceptional.interfaces.PointChangeListener;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by palfi on 2015-08-21.
 */
public class MetadataStore {

    private static final String PREFS_NAME = "metadata_preferences";
    private static final String POINTS = "points";
    private static final String EXCEPTION_VERSION = "exceptionVersion";
    private static final String LOGGED_IN = "loggedIn";
    private static final String FIRST_START_FINISHED = "firstStartFinished";
    private static final String VOTED_THIS_WEEK = "votedThisWeek";
    private static final String SUBMITTED_THIS_WEEK = "submittedThisWeek";

    @Inject Context context;
    private boolean initialized = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int points = 100;
    private int exceptionVersion = 0;
    private boolean loggedIn = false;
    private boolean firstStartFinished = false;
    private boolean votedThisWeek = true;
    private boolean submittedThisWeek = true;
    private Set<FirstStartFinishedListener> firstStartFinishedListeners = new HashSet<>();
    private Set<PointChangeListener> pointChangeListeners = new HashSet<>();

    public MetadataStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        sharedPreferences = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
        editor = sharedPreferences.edit();
        initMetadata();
        initialized = true;
    }

    public void initialize( ) {
    }

    private void initMetadata( ) {
        points = sharedPreferences.getInt( POINTS, points );
        exceptionVersion = sharedPreferences.getInt( EXCEPTION_VERSION, exceptionVersion );
        loggedIn = sharedPreferences.getBoolean( LOGGED_IN, loggedIn );
        firstStartFinished = sharedPreferences.getBoolean( FIRST_START_FINISHED, firstStartFinished );
        votedThisWeek = sharedPreferences.getBoolean( VOTED_THIS_WEEK, votedThisWeek );
        submittedThisWeek = sharedPreferences.getBoolean( SUBMITTED_THIS_WEEK, submittedThisWeek );
        editor.apply();
    }

    public void setPoints( int points ) {
        if ( this.points != points ) {
            this.points = points;
            storeInt( POINTS, points );
            if ( Looper.myLooper() == Looper.getMainLooper() ) {
                stream( pointChangeListeners ).forEach( ( PointChangeListener listener )
                        -> listener.onPointsChanged() );
            }
        }
    }

    public int getPoints( ) {
        return points;
    }

    public void setExceptionVersion( int exceptionVersion ) {
        if ( this.exceptionVersion != exceptionVersion ) {
            this.exceptionVersion = exceptionVersion;
            storeInt( EXCEPTION_VERSION, exceptionVersion );
        }
    }

    public int getExceptionVersion( ) {
        return exceptionVersion;
    }

    public void setFirstStartFinished( boolean firstStartFinished ) {
        if ( this.firstStartFinished != firstStartFinished ) {
            this.firstStartFinished = firstStartFinished;
            storeBoolean( FIRST_START_FINISHED, firstStartFinished );
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
            storeBoolean( LOGGED_IN, loggedIn );
        }
    }

    public boolean isLoggedIn( ) {
        return loggedIn;
    }

    public void setVotedThisWeek( boolean votedThisWeek ) {
        if ( this.votedThisWeek != votedThisWeek ) {
            this.votedThisWeek = votedThisWeek;
            storeBoolean( VOTED_THIS_WEEK, votedThisWeek );
        }
    }

    public boolean isVotedThisWeek( ) {
        return votedThisWeek;
    }

    public void setSubmittedThisWeek( boolean submittedThisWeek ) {
        if ( this.submittedThisWeek != submittedThisWeek ) {
            this.submittedThisWeek = submittedThisWeek;
            storeBoolean( SUBMITTED_THIS_WEEK, submittedThisWeek );
        }
    }

    public boolean isSubmittedThisWeek( ) {
        return submittedThisWeek;
    }

    private void storeInt( String key, int value ) {
        editor.putInt( key, value );
        editor.apply();
    }

    private void storeBoolean( String key, boolean value ) {
        editor.putBoolean( key, value );
        editor.apply();
    }

    public void wipe( ) {
        points = 100;
        exceptionVersion = 0;
        loggedIn = false;
        firstStartFinished = false;
        editor.clear().apply();
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
