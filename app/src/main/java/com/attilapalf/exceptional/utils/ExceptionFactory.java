package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.provider.Settings;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.model.ExceptionType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionFactory {
    private static List<ExceptionType> exceptionTypesByName = new ArrayList<>();
    private static List<ExceptionType> exceptionTypesById = new ArrayList<>();

    public static void initialize(Context context) {
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


    public static Exception createRandomException(long fromWho, long toWho, long instanceId) {
        int random = (int)(Math.random() * exceptionTypesByName.size());
        ExceptionType type = exceptionTypesByName.get(random);

        Exception e = new Exception();
        e.setFromWho(fromWho);
        e.setToWho(toWho);
        e.setDate(Calendar.getInstance());
        e.setInstanceId(instanceId);
        e.setExceptionType(type);

        return e;
    }

    public static ExceptionType findById(int id) {
        return exceptionTypesById.get(id);
    }

}
