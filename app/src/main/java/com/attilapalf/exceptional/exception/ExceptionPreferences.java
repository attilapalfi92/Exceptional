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
    public final int STORE_SIZE = Integer.MAX_VALUE;

    private static ExceptionPreferences instance;

    public static ExceptionPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new ExceptionPreferences(context);
        }

        return instance;
    }

    private ExceptionPreferences(Context context) {
        String PREFS_NAME = context.getString(R.string.exception_preferences);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();
        editor.apply();

        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> keys = store.keySet();

        String[] keyArray = new String[keys.size()];
        keys.toArray(keyArray);
        for (String s : keyArray) {
            String excJson = (String) store.get(s);
            Exception e = Exception.fromString(excJson);
            storedExceptions.addLast(e);
        }

        Collections.sort(storedExceptions, new Exception.DateComparator());
    }



    public int exceptionCount() {
        return storedExceptions.size();
    }




    public void addException(Exception e) {
//        Collections.sort(storedExceptions, new Exception.DateComparator());
        if (storedExceptions.size() >= STORE_SIZE) {
            removeLast();
        }
        storedExceptions.addFirst(e);
        editor.putString(e.getInstanceId(), e.toString());
        editor.apply();
    }


    private void removeLast() {
        Exception removed = storedExceptions.removeLast();
        editor.remove(removed.getInstanceId());
    }


    public Exception getException(int id) {
        String exception_json = sharedPreferences.getString(Integer.toString(id), "");
        return Exception.fromString(exception_json);
    }


    public void removeException(Exception e) {
        editor.remove(e.getInstanceId());
        editor.apply();
        for (int i = 0; i < storedExceptions.size(); i++) {
            if (storedExceptions.get(i).getInstanceId().equals(e.getInstanceId())) {
                storedExceptions.remove(i);
                return;
            }
        }
    }



    public List<Exception> getExceptionList() {
        return storedExceptions;
    }
}
