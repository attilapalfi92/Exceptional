package com.attilapalf.exceptional.exception;

import android.content.Context;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.utils.LoginManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionFactory {
    private static ArrayList<Exception> exceptions = new ArrayList<>();

    public static void initialize(Context context) {
        String[] strings = context.getResources().getStringArray(R.array.exceptions);

        for(String s : strings) {
            String[] parts = s.split(":Ł:");

            Exception e = new Exception();

            e.setExceptionId(Integer.parseInt(parts[0]));

            String[] names = parts[1].split(":Đ:");
            e.setPrefix(names[0]);
            e.setShortName(names[1]);

            e.setDescription(parts[2]);

            exceptions.add(e);
        }

        Collections.sort(exceptions, new Exception.ShortNameComparator());
    }


    public static Exception createRandomException(ExceptionPreferences preferences) {
        int random = (int)(Math.random() * exceptions.size());
        Exception base = exceptions.get(random);

        Exception newInstance = base.clone();
        newInstance.setFromWho("Your device");
        newInstance.setToWho("You");
        newInstance.setDate(Calendar.getInstance());

        String profileId = LoginManager.getProfilId();
        String excCount = String.valueOf(preferences.exceptionCount());
        String instanceId = profileId + excCount;
        try {
            newInstance.setInstanceId(instanceId);

        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
        }
        return newInstance;
    }

}
