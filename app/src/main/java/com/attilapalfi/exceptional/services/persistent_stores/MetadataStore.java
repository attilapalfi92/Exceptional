package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener;
import com.attilapalfi.exceptional.interfaces.PointChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.annimon.stream.Stream.of;

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

    private static boolean initialized = false;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static int points = 100;
    private static int exceptionVersion = 0;
    private static boolean loggedIn = false;
    private static boolean firstStartFinished = false;
    private static boolean votedThisWeek = true;
    private static boolean submittedThisWeek = true;
    private static Set<FirstStartFinishedListener> firstStartFinishedListeners = new HashSet<>();
    private static Set<PointChangeListener> pointChangeListeners = new HashSet<>();

    private MetadataStore() {
    }

    public static void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initMetadata();
        initialized = true;
    }

    private static void initMetadata() {
        points = sharedPreferences.getInt(POINTS, points);
        exceptionVersion = sharedPreferences.getInt(EXCEPTION_VERSION, exceptionVersion);
        loggedIn = sharedPreferences.getBoolean(LOGGED_IN, loggedIn);
        firstStartFinished = sharedPreferences.getBoolean(FIRST_START_FINISHED, firstStartFinished);
        votedThisWeek = sharedPreferences.getBoolean(VOTED_THIS_WEEK, votedThisWeek);
        submittedThisWeek = sharedPreferences.getBoolean(SUBMITTED_THIS_WEEK, submittedThisWeek);
        editor.apply();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void setPoints(int points) {
        if (MetadataStore.points != points) {
            MetadataStore.points = points;
            storeInt(POINTS, points);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                of(pointChangeListeners).forEach((PointChangeListener listener)
                        -> listener.onPointsChanged());
            }
        }
    }

    public static int getPoints() {
        return points;
    }

    public static void setExceptionVersion(int exceptionVersion) {
        if (MetadataStore.exceptionVersion != exceptionVersion) {
            MetadataStore.exceptionVersion = exceptionVersion;
            storeInt(EXCEPTION_VERSION, exceptionVersion);
        }
    }

    public static int getExceptionVersion() {
        return exceptionVersion;
    }

    public static void setFirstStartFinished(boolean firstStartFinished) {
        if (MetadataStore.firstStartFinished != firstStartFinished) {
            MetadataStore.firstStartFinished = firstStartFinished;
            storeBoolean(FIRST_START_FINISHED, firstStartFinished);
        }
        for (FirstStartFinishedListener l : firstStartFinishedListeners) {
            l.onFirstStartFinished(firstStartFinished);
        }
    }

    public static boolean isFirstStartFinished() {
        return firstStartFinished;
    }

    public static void setLoggedIn(boolean loggedIn) {
        if (MetadataStore.loggedIn != loggedIn) {
            MetadataStore.loggedIn = loggedIn;
            storeBoolean(LOGGED_IN, loggedIn);
        }
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static void setVotedThisWeek(boolean votedThisWeek) {
        if (MetadataStore.votedThisWeek != votedThisWeek) {
            MetadataStore.votedThisWeek = votedThisWeek;
            storeBoolean(VOTED_THIS_WEEK, votedThisWeek);
        }
    }

    public static boolean isVotedThisWeek() {
        return votedThisWeek;
    }

    public static void setSubmittedThisWeek(boolean submittedThisWeek) {
        if (MetadataStore.submittedThisWeek != submittedThisWeek) {
            MetadataStore.submittedThisWeek = submittedThisWeek;
            storeBoolean(SUBMITTED_THIS_WEEK, submittedThisWeek);
        }
    }

    public static boolean isSubmittedThisWeek() {
        return submittedThisWeek;
    }

    private static void storeInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    private static void storeBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void wipe() {
        points = 100;
        exceptionVersion = 0;
        loggedIn = false;
        firstStartFinished = false;
        editor.clear().apply();
    }

    public static boolean addFirstStartFinishedListener(FirstStartFinishedListener listener) {
        return firstStartFinishedListeners.add(listener);
    }

    public static boolean removeFirstStartFinishedListener(FirstStartFinishedListener listener) {
        return firstStartFinishedListeners.remove(listener);
    }

    public static boolean addPointChangeListener(PointChangeListener listener) {
        return pointChangeListeners.add(listener);
    }

    public static boolean removePointChangeListener(PointChangeListener listener) {
        return pointChangeListeners.remove(listener);
    }
}
