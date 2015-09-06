package com.attilapalfi.exceptional.ui.main;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.attilapalfi.exceptional.ui.main.friends_page.FriendsFragment;

/**
 * Created by Attila on 2015-06-07.
 */
public class MainPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
    private Activity activity;

    public MainPagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MainFragment();
            case 1:
                return new FriendsFragment();
            case 2:
                return new VotedExceptionsFragment();
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
        return super.getPageTitle(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
            switch (position) {
            case 0:
                activity.setTitle("Your profile");
                break;
            case 1:
                activity.setTitle("Friends");
                break;
            case 2:
                activity.setTitle("Voting");
                break;
            default:
                activity.setTitle("WTF happened");
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
