package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.attilapalfi.exceptional.services.persistent_stores.ExceptionTypeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by palfi on 2015-08-30.
 */
public class ExceptionTypePagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
    private List<String> exceptionTypes = new ArrayList<>();
    private Activity activity;

    public ExceptionTypePagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        this.activity = activity;
        exceptionTypes.addAll(ExceptionTypeManager.getExceptionTypes());
    }

    @Override
    public Fragment getItem(int position) {
        return new ExceptionTypesFragment();
    }

    @Override
    public int getCount() {
        return exceptionTypes.size();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position < exceptionTypes.size()) {
            activity.setTitle(exceptionTypes.get(position));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
