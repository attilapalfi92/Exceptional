package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionTypePagerAdapter extends FragmentPagerAdapter {
    private List<String> exceptionTypes;

    public ExceptionTypePagerAdapter(FragmentManager fm) {
        super(fm);
        exceptionTypes.addAll(ExceptionTypeManager.getInstance().getExceptionTypes());
    }

    @Override
    public Fragment getItem(int position) {
        if (position < exceptionTypes.size()) {
            ExceptionTypeFragment fragment = new ExceptionTypeFragment();
            fragment.setExceptionTypes(ExceptionTypeManager.getInstance().getExceptionTypesByName(exceptionTypes.get(position)));
            return fragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
