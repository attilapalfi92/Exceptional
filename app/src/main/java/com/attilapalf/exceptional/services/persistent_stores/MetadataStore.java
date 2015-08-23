package com.attilapalf.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by palfi on 2015-08-21.
 */
public class MetadataStore implements Wipeable {
    private static MetadataStore instance;

    private static final String PREFS_NAME = "metadata_preferences";
    private static final String POINTS = "points";
    private static final String EXCEPTION_VERSION = "exceptionVersion";
    private static final String LOGGED_IN = "loggedIn";
    private static final String FIRST_START_FINISHED = "firstStartFinished";

    private boolean initialized = false;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int points = 100;
    private int exceptionVersion = 0;
    private boolean loggedIn = false;
    private boolean firstStartFinished = false;

    private MetadataStore() {
    }

    public static MetadataStore getInstance() {
        if (instance == null) {
            instance = new MetadataStore();
        }
        return instance;
    }

    public void initialize(Context context) {
        sharedPreferences = context.getSharedPreferences(instance.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initMetadata();
        initialized = true;
    }

    private void initMetadata() {
        points = sharedPreferences.getInt(POINTS, points);
        exceptionVersion = sharedPreferences.getInt(EXCEPTION_VERSION, exceptionVersion);
        loggedIn = sharedPreferences.getBoolean(LOGGED_IN, loggedIn);
        firstStartFinished = sharedPreferences.getBoolean(FIRST_START_FINISHED, firstStartFinished);
        editor.apply();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setPoints(int points) {
        if (this.points != points) {
            this.points = points;
            storeInt(POINTS, points);
        }
    }

    public int getPoints() {
        return points;
    }

    public void setExceptionVersion(int exceptionVersion) {
        if (this.exceptionVersion != exceptionVersion) {
            this.exceptionVersion = exceptionVersion;
            storeInt(EXCEPTION_VERSION, exceptionVersion);
        }
    }

    public int getExceptionVersion() {
        return exceptionVersion;
    }

    public void setFirstStartFinished(boolean firstStartFinished) {
        if (this.firstStartFinished != firstStartFinished) {
            this.firstStartFinished = firstStartFinished;
            storeBoolean(FIRST_START_FINISHED, firstStartFinished);
        }
    }

    public boolean isFirstStartFinished() {
        return firstStartFinished;
    }

    public void setLoggedIn(boolean loggedIn) {
        if (this.loggedIn != loggedIn) {
            this.loggedIn = loggedIn;
            storeBoolean(LOGGED_IN, loggedIn);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void storeInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    private void storeBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    @Override
    public void wipe() {
        points = 100;
        exceptionVersion = 0;
        loggedIn = false;
        firstStartFinished = false;
        editor.clear().apply();
    }
}
