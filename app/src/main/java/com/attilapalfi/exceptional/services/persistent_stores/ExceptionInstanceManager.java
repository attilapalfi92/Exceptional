package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.interfaces.ExceptionSource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceManager implements ExceptionSource, Wipeable {
    private static ExceptionInstanceManager instance;

    public final int STORE_SIZE = Integer.MAX_VALUE;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String PREFS_NAME;
    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private List<Exception> storedExceptions = Collections.synchronizedList(new LinkedList<Exception>());
    private Geocoder geocoder;
    private List<BigInteger> knownIdsOnStart = new LinkedList<>();

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
        geocoder = new Geocoder(context, Locale.getDefault());
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> instanceIds = store.keySet();
        for (String id : instanceIds) {
            knownIdsOnStart.add(new BigInteger(id));
        }
        new AsyncExceptionLoader(storedExceptions, store).execute();
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

    public void addException(final Exception e, final boolean notifyNeeded) {
        if (!storedExceptions.contains(e)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    setCityForException();
                    addToMemoryStore();
                    editor.putString(e.getInstanceId().toString(), e.toString());
                    editor.apply();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    notifyListeners();
                }

                private void setCityForException() {
                    try {
                        e.setCity(geocoder.getFromLocation(e.getLatitude(), e.getLongitude(), 1).get(0).getLocality());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                private void addToMemoryStore() {
                    if (storedExceptions.size() >= STORE_SIZE) {
                        removeLast();
                    }
                    storedExceptions.add(0, e);
                }

                private void notifyListeners() {
                    if (notifyNeeded) {
                        for(ExceptionChangeListener listener : exceptionChangeListeners) {
                            listener.onExceptionsChanged();
                        }
                    }
                }
            }.execute();
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

    public List<BigInteger> getKnownIds() {
        return knownIdsOnStart;
    }

    private static class AsyncExceptionLoader extends AsyncTask<Void, Void, Void> {
        private List<Exception> resultList;
        private Map<String, ?> store;

        public AsyncExceptionLoader(List<Exception> resultList, Map<String, ?> store) {
            this.resultList = resultList;
            this.store = store;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Set<String> instanceIds = store.keySet();
            for (String s : instanceIds) {
                parseExceptionToMemoryList(store, s);
            }
            Collections.sort(resultList, new Exception.DateComparator());
            return null;
        }

        private void parseExceptionToMemoryList(Map<String, ?> store, String instanceId) {
            String excJson = (String) store.get(instanceId);
            Exception e = Exception.fromString(excJson);
            e.setExceptionType(ExceptionTypeManager.getInstance().findById(e.getExceptionTypeId()));
            if (!resultList.contains(e)) {
                resultList.add(resultList.size(), e);
            }
        }
    }
}
