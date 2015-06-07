package com.attilapalf.exceptional.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Attila on 2015-06-07.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private MainFragment mainFragment;
    private ExceptionsFragment exceptionsFragment;
    private FriendsFragment friendsFragment;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (mainFragment == null) {
                    mainFragment = new MainFragment();
                }
                return mainFragment;
            case 1:
                if (exceptionsFragment == null) {
                    exceptionsFragment = new ExceptionsFragment();
                }
                return exceptionsFragment;
            case 2:
                if (friendsFragment == null) {
                    friendsFragment = new FriendsFragment();
                }
                return friendsFragment;
            default:
                if (mainFragment == null) {
                    mainFragment = new MainFragment();
                }
                return mainFragment;
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
