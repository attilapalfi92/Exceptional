package com.attilapalf.exceptional.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.messages.ExceptionWrapper;
import com.attilapalf.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalf.exceptional.interfaces.ExceptionSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionManager implements ExceptionSource {

    /** This is the application's preferences */
    private SharedPreferences sharedPreferences;

    /** This is the application's sharedPreferences editor*/
    private SharedPreferences.Editor editor;

    private String PREFS_NAME;

    private long starterId = 0;

    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();

    /**
     * This LinkedList stores the last 30 exceptions the user got.
     * */
    //private static List<Exception> storedExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    private List<Exception> storedExceptions = new LinkedList<>();
    public final int STORE_SIZE = Integer.MAX_VALUE;


    private static ExceptionManager instance;

    public static ExceptionManager getInstance () {
        if (instance == null) {
            instance = new ExceptionManager();
        }

        return instance;
    }

    private ExceptionManager() {}

    public void initialize(Context context) {
        if (!ExceptionFactory.isInitialized()) {
            ExceptionFactory.initialize(context);
        }

        getInstance();

        PREFS_NAME = context.getString(R.string.exception_preferences);
        sharedPreferences = context.getSharedPreferences(instance.PREFS_NAME, Context.MODE_PRIVATE);

        editor = instance.sharedPreferences.edit();
        editor.apply();

        Map<String, ?> store = sharedPreferences.getAll();
        store.remove("starterId");
        Set<String> keys = store.keySet();

        String[] keyArray = new String[keys.size()];
        keys.toArray(keyArray);
        for (String s : keyArray) {
            String excJson = (String) store.get(s);
            Exception e = Exception.fromString(excJson);
            e.setExceptionType(ExceptionFactory.findById(e.getExceptionTypeId()));
            storedExceptions.add(storedExceptions.size(), e);
        }
        Collections.sort(storedExceptions, new Exception.DateComparator());
        starterId = sharedPreferences.getLong("starterId", 0);
    }

    public boolean isInitialized() {
        return sharedPreferences != null;
    }

    public int exceptionCount() {
        return storedExceptions.size();
    }

    public void saveStarterId(long myId) {
        starterId = myId;
        editor.putLong("starterId", myId);
        editor.apply();
    }

    public long getStarterId() {
        return starterId;
    }


    public long getLastKnownId() {
        if (storedExceptions.isEmpty()) {
            return starterId;
        }

        return storedExceptions.get(0).getInstanceId();
    }


    public long getNextId() {
        if (storedExceptions.isEmpty()) {
            return starterId;
        }

        return storedExceptions.get(0).getInstanceId() + 1;
    }


    public void addException(Exception e, boolean notifyNeeded) {
        if (storedExceptions.size() >= STORE_SIZE) {
            removeLast();
        }
        //storedExceptions.addFirst(e);
        storedExceptions.add(0, e);
        editor.putString(Long.toString(e.getInstanceId()), e.toString());
        editor.apply();

        if (notifyNeeded) {
            for(ExceptionChangeListener listener : exceptionChangeListeners) {
                listener.onExceptionsChanged();
            }
        }
    }


    public void addExceptions(List<ExceptionWrapper> wrapperList) {
        for (ExceptionWrapper wrapper : wrapperList) {
            Exception e = new Exception(wrapper);
            addException(e, false);
        }

        for(ExceptionChangeListener listener : exceptionChangeListeners) {
            listener.onExceptionsChanged();
        }
    }


    private void removeLast() {
        //Exception removed = storedExceptions.removeLast();
        Exception removed = storedExceptions.remove(storedExceptions.size() - 1);
        editor.remove(Long.toString(removed.getInstanceId()));
    }



    public void removeException(Exception e) {
        editor.remove(Long.toString(e.getInstanceId()));
        editor.apply();
        for (int i = 0; i < storedExceptions.size(); i++) {
            if (storedExceptions.get(i).getInstanceId() == e.getInstanceId()) {
                storedExceptions.remove(i);
                return;
            }
        }
    }


    public List<Exception> getExceptionList() {
        return storedExceptions;
    }

    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.add(listener);
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.remove(listener);
    }
}
