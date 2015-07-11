package com.attilapalf.exceptional.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.messages.ExceptionWrapper;
import com.attilapalf.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalf.exceptional.interfaces.ExceptionSource;

import java.util.ArrayList;
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

        new AsyncExceptionLoader(storedExceptions, sharedPreferences, editor).execute();
    }



    private static class AsyncExceptionLoader extends AsyncTask<Void, Void, Void> {

        private List<Exception> resultList;
        private List<Exception> temporaryList;
        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;

        public AsyncExceptionLoader(List<Exception> resultList, SharedPreferences sharedPreferences,
                                    SharedPreferences.Editor editor) {
            this.resultList = resultList;
            this.sharedPreferences = sharedPreferences;
            this.editor = editor;
            temporaryList = new LinkedList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {

            Map<String, ?> store = sharedPreferences.getAll();
            Set<String> keys = store.keySet();

            String[] keyArray = new String[keys.size()];
            keys.toArray(keyArray);
            for (String s : keyArray) {
                String excJson = (String) store.get(s);
                Exception e = Exception.fromString(excJson);
                e.setExceptionType(ExceptionFactory.findById(e.getExceptionTypeId()));
                temporaryList.add(temporaryList.size(), e);
            }
            Collections.sort(temporaryList, new Exception.DateComparator());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (Exception e : temporaryList) {
                resultList.add(e);
            }
            temporaryList = null;
        }
    }





    public boolean isInitialized() {
        return sharedPreferences != null;
    }


    public int exceptionCount() {
        return storedExceptions.size();
    }



    public long getLastKnownId() {
        if (storedExceptions.isEmpty()) {
            return 0;
        }

        return storedExceptions.get(0).getInstanceId();
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
