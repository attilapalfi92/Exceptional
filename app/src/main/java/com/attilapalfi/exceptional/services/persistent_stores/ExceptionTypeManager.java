package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionType;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeManager implements Wipeable {
    private static ExceptionTypeManager instance;
    private static final String PREFS_NAME = "exception_types";
    private static final String MIN_ID = "minId";
    private static final String MAX_ID = "maxId";
    private static final String HAS_DATA = "hasData";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Map<String, List<ExceptionType>> exceptionTypeStore;
    private List<ExceptionType> votedExceptionTypeStore;

    private ExceptionTypeManager() {}

    public static ExceptionTypeManager getInstance() {
        if (instance == null) {
            instance = new ExceptionTypeManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        initPreferences(context);
        exceptionTypeStore = new HashMap<>();
        if (sharedPreferences.getBoolean(HAS_DATA, false)) {
            initExceptionTypeStore();
            sortExceptionStore();
        }
    }

    public void addExceptionTypes(List<ExceptionType> exceptionTypes) {
        loadExceptionTypeStore(exceptionTypes);
        editor.putBoolean(HAS_DATA, true);
        editor.apply();
    }

    private void loadExceptionTypeStore(List<ExceptionType> exceptionTypes) {
        int maxId = 0;
        int minId = Integer.MAX_VALUE;
        for (ExceptionType exception : exceptionTypes) {
            if (!exceptionTypeStore.containsKey(exception.getType())) {
                exceptionTypeStore.put(exception.getType(), new ArrayList<ExceptionType>());
            }
            exceptionTypeStore.get(exception.getType()).add(exception);
            editor.putString(Integer.toString(exception.getId()), exception.toString());
            minId = exception.getId() < minId ? exception.getId() : minId;
            maxId = exception.getId() > maxId ? exception.getId() : maxId;
        }
        editor.putInt(MIN_ID, minId);
        editor.putInt(MAX_ID, maxId);
        editor.apply();
        sortExceptionStore();
    }

    public void setVotedExceptionTypes(List<ExceptionType> exceptionTypes) {
        votedExceptionTypeStore = exceptionTypes;
        sortVotedExceptionStore();
    }

    private void initPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(instance.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    private void initExceptionTypeStore() {
        int minId = sharedPreferences.getInt(MIN_ID, 0);
        int maxId = sharedPreferences.getInt(MAX_ID, 0);
        for (int i = minId; i <= maxId; i++) {
            ExceptionType exceptionType = ExceptionType.fromString(sharedPreferences.getString(Integer.toString(i), ""));
            if (!exceptionTypeStore.containsKey(exceptionType.getType())) {
                exceptionTypeStore.put(exceptionType.getType(), new ArrayList<ExceptionType>());
            }
            exceptionTypeStore.get(exceptionType.getType()).add(exceptionType);
        }
    }

    private void sortExceptionStore() {
        for(List<ExceptionType> typeList : exceptionTypeStore.values()) {
            Collections.sort(typeList, new ExceptionType.ShortNameComparator());
        }
    }

    private void sortVotedExceptionStore() {
        Collections.sort(votedExceptionTypeStore, new ExceptionType.VoteComparator());
    }


    public Exception createException(int typeId, BigInteger fromWho, BigInteger toWho) {
        ExceptionType type = findById(typeId);
        return createInstanceWithType(fromWho, toWho, type);
    }

    public Exception createRandomException(BigInteger fromWho, BigInteger toWho) {
        ExceptionType exceptionType = new ExceptionType();
        int random = (int)(Math.random() * exceptionTypeStore.keySet().size());
        int counter = 0;
        for (String type : exceptionTypeStore.keySet()) {
            if (random == counter++) {
                random = (int)(Math.random() * exceptionTypeStore.get(type).size());
                exceptionType = exceptionTypeStore.get(type).get(random);
            }
        }
        return createInstanceWithType(fromWho, toWho, exceptionType);
    }

    private Exception createInstanceWithType(BigInteger fromWho, BigInteger toWho, ExceptionType type) {
        Exception exception = new Exception();
        exception.setFromWho(fromWho);
        exception.setToWho(toWho);
        exception.setDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        exception.setExceptionType(type);
        return exception;
    }

    public ExceptionType findById(int id) {
        for (List<ExceptionType> exceptionTypeList : exceptionTypeStore.values()) {
            for (ExceptionType exceptionType : exceptionTypeList) {
                if (id == exceptionType.getId()) {
                    return exceptionType;
                }
            }
        }
        return new ExceptionType();
    }

    public Set<String> getExceptionTypes() {
        return exceptionTypeStore.keySet();
    }

    public boolean isInitialized() {
        return exceptionTypeStore != null;
    }

    public List<ExceptionType> getExceptionTypesByName(String typeName) {
        if (exceptionTypeStore.containsKey(typeName)) {
            return new ArrayList<>(exceptionTypeStore.get(typeName));
        }
        return new ArrayList<>();
    }

    @Override
    public void wipe() {
        exceptionTypeStore.clear();
        votedExceptionTypeStore.clear();
        editor.clear().apply();
    }
}
