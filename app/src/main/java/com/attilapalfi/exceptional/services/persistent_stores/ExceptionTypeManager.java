package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import com.attilapalfi.exceptional.interfaces.VotedTypeListener;
import com.attilapalfi.exceptional.model.ExceptionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.annimon.stream.Stream.of;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeManager {
    private static final String PREFS_NAME = "exception_types";
    private static final String MIN_ID = "minId";
    private static final String MAX_ID = "maxId";
    private static final String HAS_DATA = "hasData";

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static Map<String, List<ExceptionType>> exceptionTypeStore;
    private static List<ExceptionType> votedExceptionTypeList;
    private static Set<VotedTypeListener> votedTypeListeners = new HashSet<>();

    private ExceptionTypeManager() {}

    public static void initialize(Context context) {
        initPreferences(context);
        exceptionTypeStore = new HashMap<>();
        if (sharedPreferences.getBoolean(HAS_DATA, false)) {
            initExceptionTypeStore();
            sortExceptionStore();
        }
    }

    public static void addExceptionTypes(List<ExceptionType> exceptionTypes) {
        loadExceptionTypeStore(exceptionTypes);
        editor.putBoolean(HAS_DATA, true);
        editor.apply();
    }

    private static void loadExceptionTypeStore(List<ExceptionType> exceptionTypes) {
        int maxId = 0;
        for (ExceptionType exception : exceptionTypes) {
            if (!exceptionTypeStore.containsKey(exception.getType())) {
                exceptionTypeStore.put(exception.getType(), new ArrayList<>());
            }
            exceptionTypeStore.get(exception.getType()).add(exception);
            editor.putString(Integer.toString(exception.getId()), exception.toString());
            maxId = exception.getId() > maxId ? exception.getId() : maxId;
        }
        editor.putInt(MAX_ID, maxId);
        editor.apply();
        sortExceptionStore();
    }

    public static void setVotedExceptionTypes(List<ExceptionType> exceptionTypes) {
        votedExceptionTypeList = exceptionTypes;
        sortVotedExceptionList();
        notifyVotedTypeListeners();
    }

    private static void initPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    private static void initExceptionTypeStore() {
        int minId = sharedPreferences.getInt(MIN_ID, 1);
        int maxId = sharedPreferences.getInt(MAX_ID, 0);
        for (int i = minId; i <= maxId; i++) {
            ExceptionType exceptionType = ExceptionType.fromString(sharedPreferences.getString(Integer.toString(i), ""));
            if (!exceptionTypeStore.containsKey(exceptionType.getType())) {
                exceptionTypeStore.put(exceptionType.getType(), new ArrayList<>());
            }
            exceptionTypeStore.get(exceptionType.getType()).add(exceptionType);
        }
    }

    private static void sortExceptionStore() {
        for(List<ExceptionType> typeList : exceptionTypeStore.values()) {
            Collections.sort(typeList, new ExceptionType.ShortNameComparator());
        }
    }

    private static void sortVotedExceptionList() {
        Collections.sort(votedExceptionTypeList, new ExceptionType.VoteComparator());
    }

    public static ExceptionType findById(int id) {
        for (List<ExceptionType> exceptionTypeList : exceptionTypeStore.values()) {
            for (ExceptionType exceptionType : exceptionTypeList) {
                if (id == exceptionType.getId()) {
                    return exceptionType;
                }
            }
        }
        return new ExceptionType();
    }

    public static Set<String> getExceptionTypes() {
        return exceptionTypeStore.keySet();
    }

    public static boolean isInitialized() {
        return exceptionTypeStore != null;
    }

    public static List<ExceptionType> getExceptionTypeListByName(String typeName) {
        if (exceptionTypeStore.containsKey(typeName)) {
            return new ArrayList<>(exceptionTypeStore.get(typeName));
        }
        return new ArrayList<>();
    }

    public static void wipe() {
        exceptionTypeStore.clear();
        votedExceptionTypeList.clear();
        editor.clear().apply();
    }

    public static List<ExceptionType> getVotedExceptionTypeList() {
        return votedExceptionTypeList;
    }

    public static void updateVotedType(ExceptionType votedType) {
        int index = votedExceptionTypeList.indexOf(votedType);
        ExceptionType listElementException = votedExceptionTypeList.get(index);
        listElementException.setVoteCount(votedType.getVoteCount());
        notifyVotedTypeListeners();
    }

    public static void addVotedType(ExceptionType submittedType) {
        votedExceptionTypeList.add(submittedType);
        notifyVotedTypeListeners();
    }

    public static void notifyVotedTypeListeners() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            of(votedTypeListeners).forEach(VotedTypeListener::onVoteListChanged);
        }
    }

    public static boolean addVotedTypeListener(VotedTypeListener listener) {
        return votedTypeListeners.add(listener);
    }

    public static boolean removeVotedTypeListener(VotedTypeListener listener) {
        return votedTypeListeners.remove(listener);
    }
}
