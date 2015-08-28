package com.attilapalfi.exceptional.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.attilapalfi.exceptional.ui.main.friends_page.FriendsFragment2;

/**
 * Created by Attila on 2015-06-07.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MainFragment();
            case 1:
                return new ExceptionsFragment2();
            case 2:
                return new FriendsFragment2();
            default:
                return new MainFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Main";
            case 1:
                return "My Exceptions";
            case 2:
                return "My Friends";
            default:
                return "unknown";
        }
    }
}
