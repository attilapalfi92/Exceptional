package com.attilapalf.exceptional.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.model.ExceptionType;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeManager {
    private static ExceptionTypeManager instance;
    private static final String PREFS_NAME = "exception_types";
    private List<ExceptionType> exceptionTypesByName;
    private List<ExceptionType> exceptionTypesById;
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

    public void addExceptionTypes(List<ExceptionType> exceptionTypes) {
        loadExceptionTypeStore(exceptionTypes);
    }

    public void addExceptionTypes2(List<ExceptionType> exceptionTypes) {
        loadExceptionTypeStore(exceptionTypes);
    }

    private void loadExceptionTypeStore(List<ExceptionType> exceptionTypes) {
        for (ExceptionType exception : exceptionTypes) {
            if (!exceptionTypeStore.containsKey(exception.getType())) {
                exceptionTypeStore.put(exception.getType(), new ArrayList<ExceptionType>());
            }
            exceptionTypeStore.get(exception.getType()).add(exception);
            editor.putString(Integer.toString(exception.getId()), exception.toString());
        }
        editor.apply();
        sortExceptionStore();
    }

    public void setVotedExceptionTypes(List<ExceptionType> exceptionTypes) {
        votedExceptionTypeStore = exceptionTypes;
        sortVotedExceptionStore();
    }

    public void initialize(Context context) {
        initPreferences(context);
        if (sharedPreferences.getBoolean("hasData", false)) {
            initExceptionTypeStore();
            fillExceptionStore();
            sortExceptionStore();
        }
    }

    private void initPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(instance.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    // TODO: remove this method
    private void initExceptionTypeStore() {
        exceptionTypeStore = new HashMap<>();
        Set<String> types = sharedPreferences.getStringSet("types", new HashSet<String>());
        for (String type : types) {
            exceptionTypeStore.put(type, new ArrayList<ExceptionType>());
        }
    }

    // TODO: save maxId where it has to be saved
    // TODO: store exception version in this class
    private void fillExceptionStore() {
        int maxId = sharedPreferences.getInt("maxId", 0);
        for (int i = 0; i < maxId; i++) {
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
        Exception e = new Exception();
        ExceptionType type = findById(typeId);
        createInstance(fromWho, toWho, e, type);
        return e;
    }

    public Exception createRandomException(BigInteger fromWho, BigInteger toWho) {
        int random = (int)(Math.random() * exceptionTypesByName.size());
        ExceptionType type = exceptionTypesByName.get(random);
        Exception e = new Exception();
        createInstance(fromWho, toWho, e, type);
        return e;
    }

    private void createInstance(BigInteger fromWho, BigInteger toWho, Exception e, ExceptionType type) {
        e.setFromWho(fromWho);
        e.setToWho(toWho);
        e.setDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        e.setExceptionType(type);
    }


    public ExceptionType findById(int id) {
        return exceptionTypesById.get(id);
    }

    public boolean isInitialized() {
        return exceptionTypeStore != null;
    }

    public List<ExceptionType> getExceptionTypesByName() {
        return exceptionTypeStore.get("JAVA");
    }
}
