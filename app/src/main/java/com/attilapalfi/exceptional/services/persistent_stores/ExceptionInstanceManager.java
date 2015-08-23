package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.interfaces.ExceptionSource;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceManager implements ExceptionSource, Wipeable {

    public final int STORE_SIZE = Integer.MAX_VALUE;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String PREFS_NAME;
    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private List<Exception> storedExceptions = new LinkedList<>();
    private static ExceptionInstanceManager instance;

    public static ExceptionInstanceManager getInstance () {
        if (instance == null) {
            instance = new ExceptionInstanceManager();
        }

        return instance;
    }

    private ExceptionInstanceManager() {}

    public void initialize(Context context) {
        if (!ExceptionTypeManager.getInstance().isInitialized()) {
            ExceptionTypeManager.getInstance().initialize(context);
        }
        PREFS_NAME = context.getString(R.string.exception_preferences);
        sharedPreferences = context.getSharedPreferences(instance.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
        new AsyncExceptionLoader(storedExceptions, sharedPreferences).execute();
    }

    @Override
    public void wipe() {
        storedExceptions.clear();
        editor.clear().apply();
        for (ExceptionChangeListener listener : exceptionChangeListeners) {
            listener.onExceptionsChanged();
        }
    }

    public boolean isInitialized() {
        return sharedPreferences != null;
    }


    public int exceptionCount() {
        return storedExceptions.size();
    }



    public BigInteger getLastKnownId() {
        if (storedExceptions.isEmpty()) {
            return new BigInteger("0");
        }

        return storedExceptions.get(0).getInstanceId();
    }



    public void addException(Exception e, boolean notifyNeeded) {
        if (storedExceptions.size() >= STORE_SIZE) {
            removeLast();
        }
        //storedExceptions.addFirst(e);
        storedExceptions.add(0, e);
        editor.putString(e.getInstanceId().toString(), e.toString());
        editor.apply();

        if (notifyNeeded) {
            for(ExceptionChangeListener listener : exceptionChangeListeners) {
                listener.onExceptionsChanged();
            }
        }
    }


    public void saveExceptions(List<ExceptionInstanceWrapper> wrapperList) {
        for (ExceptionInstanceWrapper wrapper : wrapperList) {
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
        editor.remove(removed.getInstanceId().toString());
    }



    public void removeException(Exception e) {
        editor.remove(e.getInstanceId().toString());
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

    @Override
    public boolean addExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.add(listener);
    }

    @Override
    public boolean removeExceptionChangeListener(ExceptionChangeListener listener) {
        return exceptionChangeListeners.remove(listener);
    }

    private static class AsyncExceptionLoader extends AsyncTask<Void, Void, Void> {

        private List<Exception> resultList;
        private List<Exception> temporaryList;
        private SharedPreferences sharedPreferences;

        public AsyncExceptionLoader(List<Exception> resultList, SharedPreferences sharedPreferences) {
            this.resultList = resultList;
            this.sharedPreferences = sharedPreferences;
            temporaryList = new LinkedList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Map<String, ?> store = sharedPreferences.getAll();
            Set<String> keys = store.keySet();
            for (String s : keys) {
                parseExceptionToTemporaryList(store, s);
            }
            Collections.sort(temporaryList, new Exception.DateComparator());
            return null;
        }

        private void parseExceptionToTemporaryList(Map<String, ?> store, String exceptionString) {
            String excJson = (String) store.get(exceptionString);
            Exception e = Exception.fromString(excJson);
            e.setExceptionType(ExceptionTypeManager.getInstance().findById(e.getExceptionTypeId()));
            temporaryList.add(temporaryList.size(), e);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (Exception e : temporaryList) {
                resultList.add(e);
            }
            temporaryList = null;
        }
    }
}
