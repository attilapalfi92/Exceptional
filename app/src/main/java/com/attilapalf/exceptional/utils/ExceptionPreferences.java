package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;

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
    private static SharedPreferences sharedPreferences;

    /** This is the application's sharedPreferences editor*/
    private static SharedPreferences.Editor editor;

    /**
     * This LinkedList stores the last 30 exceptions the user got.
     * */
    // TODO: synchronize this motherfucker
    private static List<Exception> storedExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    public static final int STORE_SIZE = Integer.MAX_VALUE;

    public static void initalize(Context context) {
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
            storedExceptions.add(storedExceptions.size(), e);
            //storedExceptions.addLast(e);
        }

        Collections.sort(storedExceptions, new Exception.DateComparator());
    }



    public static int exceptionCount() {
        return storedExceptions.size();
    }




    public static void addException(Exception e) {
        if (storedExceptions.size() >= STORE_SIZE) {
            removeLast();
        }
        //storedExceptions.addFirst(e);
        storedExceptions.add(0, e);
        editor.putString(e.getInstanceId(), e.toString());
        editor.apply();
    }


    private static void removeLast() {
        //Exception removed = storedExceptions.removeLast();
        Exception removed = storedExceptions.remove(storedExceptions.size() - 1);
        editor.remove(removed.getInstanceId());
    }


    public static Exception getException(int id) {
        String exception_json = sharedPreferences.getString(Integer.toString(id), "");
        return Exception.fromString(exception_json);
    }


    public static void removeException(Exception e) {
        editor.remove(e.getInstanceId());
        editor.apply();
        for (int i = 0; i < storedExceptions.size(); i++) {
            if (storedExceptions.get(i).getInstanceId().equals(e.getInstanceId())) {
                storedExceptions.remove(i);
                return;
            }
        }
    }


    public static List<Exception> getExceptionList() {
        return storedExceptions;
    }
}
