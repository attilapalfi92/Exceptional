package com.attilapalf.exceptional.services;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by palfi on 2015-08-21.
 */
public class MetadataStore {
    private static MetadataStore instance;

    private static final String PREFS_NAME = "metadata_preferences";
    private static final String POINTS = "points";
    private static final String EXCEPTION_VERSION = "exceptionVersion";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int points = 100;
    private int exceptionVersion = 1;

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
        points = Integer.parseInt(sharedPreferences.getString(POINTS, "100"));
        exceptionVersion = Integer.parseInt(sharedPreferences.getString(EXCEPTION_VERSION, "1"));
        editor.apply();
    }

    public void setPoints(int points) {
        this.points = points;
        storePair(POINTS, Integer.toString(points));
    }

    public int getPoints() {
        return points;
    }

    public int getExceptionVersion() {
        return exceptionVersion;
    }

    public void setExceptionVersion(int exceptionVersion) {
        this.exceptionVersion = exceptionVersion;
        storePair(EXCEPTION_VERSION, Integer.toString(exceptionVersion));
    }

    private void storePair(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
}
