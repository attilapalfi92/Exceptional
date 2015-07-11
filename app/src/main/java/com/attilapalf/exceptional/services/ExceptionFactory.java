package com.attilapalf.exceptional.services;

import android.content.Context;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.model.ExceptionType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionFactory {
    private static List<ExceptionType> exceptionTypesByName;
    private static List<ExceptionType> exceptionTypesById;

    public static void initialize(Context context) {
        exceptionTypesByName = new ArrayList<>();
        exceptionTypesById = new ArrayList<>();
        String[] strings = context.getResources().getStringArray(R.array.exceptions);

        for(String s : strings) {
            String[] parts = s.split(":Ł:");

            ExceptionType e = new ExceptionType();

            e.setTypeId(Integer.parseInt(parts[0]));

            String[] names = parts[1].split(":Đ:");
            e.setPrefix(names[0]);
            e.setShortName(names[1]);

            e.setDescription(parts[2]);

            exceptionTypesByName.add(e);
            exceptionTypesById.add(e);
        }

        Collections.sort(exceptionTypesByName, new ExceptionType.ShortNameComparator());
        Collections.sort(exceptionTypesById, new ExceptionType.IdComparator());
    }


    public static Exception createException(int typeId, long fromWho, long toWho) {

        Exception e = new Exception();
        e.setFromWho(fromWho);
        e.setToWho(toWho);
        e.setDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        e.setExceptionType(findById(typeId));

        return e;
    }

    public static Exception createRandomException(long fromWho, long toWho) {
        int random = (int)(Math.random() * exceptionTypesByName.size());
        ExceptionType type = exceptionTypesByName.get(random);

        Exception e = new Exception();
        e.setFromWho(fromWho);
        e.setToWho(toWho);
        e.setDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        e.setExceptionType(type);

        return e;
    }


    public static ExceptionType findById(int id) {
        return exceptionTypesById.get(id);
    }

    public static boolean isInitialized() {
        return exceptionTypesById != null;
    }

    public static List<ExceptionType> getExceptionTypesByName() {
        return exceptionTypesByName;
    }

}
