package com.attilapalf.exceptional.exception;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionPreferences {

    /** This is the application's preferences */
    private SharedPreferences sharedPreferences;

    /** This is the application's sharedPreferences editor*/
    private SharedPreferences.Editor editor;

    /**
     * This LinkedList stores the last 30 exceptions the user got.
     * */
    private LinkedList<Exception> storedExceptions = new LinkedList<>();
    public final int STORE_SIZE = 30;


    private static ExceptionPreferences instance;

    public static ExceptionPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new ExceptionPreferences(context);
        }

        return instance;
    }

    private ExceptionPreferences(Context context) {
        if (sharedPreferences == null) {
            String PREFS_NAME = context.getString(R.string.exception_preferences);
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        /*
        * Get a SharedPreferences editor instance.
        * SharedPreferences ensures that updates are atomic
        * and non-concurrent
        */
        editor = sharedPreferences.edit();
        editor.apply();

        // loading the @storedExceptions with data
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> keys = store.keySet();
        // if sharedPreferences stores at least 1 element, and storedExceptions is still empty
        // then we have to fill it
        if (keys.size() > 0 && storedExceptions.size() == 0) {
            String[] keyArray = new String[keys.size()];
            keys.toArray(keyArray);
            for (String s : keyArray) {
                String excJson = (String) store.get(s);
                Exception e = Exception.fromString(excJson);
                storedExceptions.addLast(e);
            }
        }

        Collections.sort(storedExceptions, new Exception.DateComparator());
    }

    public void addException(Exception e) {
//        Collections.sort(storedExceptions, new Exception.DateComparator());
        if (storedExceptions.size() >= STORE_SIZE) {
            removeLast();
        }
        storedExceptions.addFirst(e);
        editor.putString(Integer.toString(e.getId()), e.toString());
        editor.apply();
    }


    private void removeLast() {
        Exception removed = storedExceptions.removeLast();
        editor.remove(Integer.toString(removed.getId()));
    }


    public Exception getException(int id) {
        String exception_json = sharedPreferences.getString(Integer.toString(id), "");
        return Exception.fromString(exception_json);
    }


    public void removeException(Exception e) {
        editor.remove(Integer.toString(e.getId()));
        editor.apply();
        for (int i = 0; i < storedExceptions.size(); i++) {
            if (storedExceptions.get(i).getId() == e.getId()) {
                storedExceptions.remove(i);
                return;
            }
        }
    }



    public List<Exception> getExceptionList() {
        return storedExceptions;
    }
}
